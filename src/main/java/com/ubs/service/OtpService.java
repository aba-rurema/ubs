package com.ubs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.config.MailProperties;
import com.ubs.config.OtpProperties;
import com.ubs.dto.auth.LoginRequest;
import com.ubs.dto.auth.OtpChallengeResponse;
import com.ubs.dto.auth.RegisterRequest;
import com.ubs.dto.auth.VerifyOtpRequest;
import com.ubs.entity.OtpChallenge;
import com.ubs.entity.OtpPurpose;
import com.ubs.entity.User;
import com.ubs.exception.BusinessRuleViolationException;
import com.ubs.repository.OtpChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class OtpService {

	private static final Logger log = LoggerFactory.getLogger(OtpService.class);
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final OtpChallengeRepository otpChallengeRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final OtpProperties otpProperties;
	private final MailProperties mailProperties;
	private final ObjectMapper objectMapper;

	public OtpService(OtpChallengeRepository otpChallengeRepository,
					  EmailService emailService,
					  PasswordEncoder passwordEncoder,
					  OtpProperties otpProperties,
					  MailProperties mailProperties,
					  ObjectMapper objectMapper) {
		this.otpChallengeRepository = otpChallengeRepository;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
		this.otpProperties = otpProperties;
		this.mailProperties = mailProperties;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public OtpChallengeResponse initiateLogin(User user) {
		invalidateActiveChallenges(user.getEmail(), OtpPurpose.LOGIN, user.getId());
		return createAndSendChallenge(user.getEmail(), OtpPurpose.LOGIN, user, null);
	}

	@Transactional
	public OtpChallengeResponse initiateRegister(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		invalidateActiveChallenges(email, OtpPurpose.REGISTER, null);
		String payload = serializeRegisterRequest(request);
		return createAndSendChallenge(email, OtpPurpose.REGISTER, null, payload);
	}

	@Transactional(readOnly = true)
	public OtpChallenge verify(VerifyOtpRequest request, OtpPurpose expectedPurpose) {
		OtpChallenge challenge = otpChallengeRepository.findByIdAndUsedFalse(request.sessionId())
				.orElseThrow(() -> new BusinessRuleViolationException("Invalid or expired OTP session"));

		if (challenge.getPurpose() != expectedPurpose) {
			throw new BusinessRuleViolationException("OTP session does not match the requested action");
		}
		if (challenge.isExpired()) {
			throw new BusinessRuleViolationException("OTP has expired. Please request a new code.");
		}
		if (!passwordEncoder.matches(request.otpCode(), challenge.getOtpHash())) {
			throw new BusinessRuleViolationException("Invalid OTP code");
		}

		return challenge;
	}

	@Transactional
	public void markUsed(OtpChallenge challenge) {
		challenge.setUsed(true);
		otpChallengeRepository.save(challenge);
	}

	@Transactional(readOnly = true)
	public RegisterRequest deserializeRegisterPayload(OtpChallenge challenge) {
		try {
			return objectMapper.readValue(challenge.getPayload(), RegisterRequest.class);
		} catch (JsonProcessingException ex) {
			throw new BusinessRuleViolationException("Registration session is invalid. Please register again.");
		}
	}

	private OtpChallengeResponse createAndSendChallenge(String email,
														OtpPurpose purpose,
														User user,
														String payload) {
		String otpCode = generateOtpCode();
		OtpChallenge challenge = OtpChallenge.builder()
				.purpose(purpose)
				.user(user)
				.email(email)
				.otpHash(passwordEncoder.encode(otpCode))
				.payload(payload)
				.expiresAt(Instant.now().plus(otpProperties.expirationMinutes(), ChronoUnit.MINUTES))
				.build();
		OtpChallenge saved = otpChallengeRepository.save(challenge);

		sendOtpEmail(email, otpCode, purpose);
		logDevOtp(email, purpose, saved.getId(), otpCode);

		String action = purpose == OtpPurpose.LOGIN ? "sign in" : "complete your registration";
		return new OtpChallengeResponse(
				saved.getId(),
				"A 6-digit verification code has been sent to your email. Use it to " + action + ".",
				maskEmail(email),
				otpProperties.expirationMinutes(),
				purpose
		);
	}

	private void invalidateActiveChallenges(String email, OtpPurpose purpose, Long userId) {
		otpChallengeRepository.invalidateActiveByEmailAndPurpose(email, purpose);
		if (userId != null) {
			otpChallengeRepository.invalidateActiveByUserAndPurpose(userId, purpose);
		}
	}

	private void logDevOtp(String email, OtpPurpose purpose, Long sessionId, String otpCode) {
		if (mailProperties.enabled()) {
			return;
		}
		log.warn("[DEV OTP] purpose={} email={} sessionId={} code={} — paste code into verify-otp (mail disabled)",
				purpose, email, sessionId, otpCode);
	}

	private void sendOtpEmail(String email, String otpCode, OtpPurpose purpose) {
		String action = purpose == OtpPurpose.LOGIN ? "sign in" : "registration";
		String subject = "Your Utility Billing System verification code";
		String body = """
				Hello,

				Your one-time verification code for %s is:

				    %s

				This code expires in %d minutes.

				If you did not request this, you can ignore this email.

				—
				%s
				""".formatted(
				action,
				otpCode,
				otpProperties.expirationMinutes(),
				mailProperties.fromName()
		);
		emailService.sendPlainText(email, subject, body);
	}

	private String generateOtpCode() {
		int length = otpProperties.codeLength();
		int bound = (int) Math.pow(10, length);
		int min = bound / 10;
		return String.format("%0" + length + "d", SECURE_RANDOM.nextInt(bound - min) + min);
	}

	private String maskEmail(String email) {
		int atIndex = email.indexOf('@');
		if (atIndex <= 1) {
			return "***" + email.substring(Math.max(atIndex, 0));
		}
		return email.charAt(0) + "*****" + email.substring(atIndex);
	}

	private String serializeRegisterRequest(RegisterRequest request) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize registration request", ex);
		}
	}

}
