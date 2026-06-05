package com.ubs.service;

import com.ubs.audit.AuditContext;
import com.ubs.config.MailProperties;
import com.ubs.config.PasswordResetProperties;
import com.ubs.dto.auth.ChangePasswordRequest;
import com.ubs.dto.auth.ForgotPasswordRequest;
import com.ubs.dto.auth.MessageResponse;
import com.ubs.dto.auth.ResetPasswordRequest;
import com.ubs.entity.AuditAction;
import com.ubs.entity.PasswordResetToken;
import com.ubs.entity.Role;
import com.ubs.entity.User;
import com.ubs.exception.BusinessRuleViolationException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.PasswordResetTokenRepository;
import com.ubs.repository.UserRepository;
import com.ubs.security.CustomUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetService {

	private static final String GENERIC_FORGOT_PASSWORD_MESSAGE =
			"If an account exists for that email, a password reset link has been sent.";

	private final UserRepository userRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetProperties passwordResetProperties;
	private final MailProperties mailProperties;
	private final AuditLogService auditLogService;

	public PasswordResetService(UserRepository userRepository,
								  PasswordResetTokenRepository passwordResetTokenRepository,
								  EmailService emailService,
								  PasswordEncoder passwordEncoder,
								  PasswordResetProperties passwordResetProperties,
								  MailProperties mailProperties,
								  AuditLogService auditLogService) {
		this.userRepository = userRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
		this.passwordResetProperties = passwordResetProperties;
		this.mailProperties = mailProperties;
		this.auditLogService = auditLogService;
	}

	@Transactional
	public MessageResponse forgotPassword(ForgotPasswordRequest request) {
		String email = request.email().trim().toLowerCase();
		userRepository.findByEmail(email).ifPresent(user -> {
			sendResetEmail(user);
			auditLogService.log(
					AuditAction.PASSWORD_FORGOT_REQUESTED,
					"User",
					user.getId(),
					"Password reset requested for " + email,
					user.getId(),
					user.getUsername(),
					null,
					AuditContext.currentIpAddress()
			);
		});
		return new MessageResponse(GENERIC_FORGOT_PASSWORD_MESSAGE);
	}

	@Transactional
	public MessageResponse resetPassword(ResetPasswordRequest request) {
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token().trim())
				.orElseThrow(() -> new BusinessRuleViolationException("Invalid or expired reset token"));

		if (resetToken.isUsed()) {
			throw new BusinessRuleViolationException("This reset token has already been used");
		}
		if (resetToken.isExpired()) {
			throw new BusinessRuleViolationException("Reset token has expired");
		}

		User user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);

		resetToken.setUsed(true);
		passwordResetTokenRepository.save(resetToken);
		passwordResetTokenRepository.invalidateActiveTokensForUser(user);

		auditLogService.log(
				AuditAction.PASSWORD_RESET,
				"User",
				user.getId(),
				"Password reset completed for " + user.getUsername(),
				user.getId(),
				user.getUsername(),
				null,
				AuditContext.currentIpAddress()
		);

		return new MessageResponse("Password has been reset successfully. You can now log in with your new password.");
	}

	@Transactional
	public MessageResponse changePassword(CustomUserDetails userDetails, ChangePasswordRequest request) {
		User user = userRepository.findById(userDetails.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw new BusinessRuleViolationException("Current password is incorrect");
		}
		if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
			throw new BusinessRuleViolationException("New password must be different from the current password");
		}

		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
		passwordResetTokenRepository.invalidateActiveTokensForUser(user);

		auditLogService.log(
				AuditAction.PASSWORD_CHANGED,
				"User",
				user.getId(),
				"Password changed for " + user.getUsername(),
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getRoles().stream().findFirst().map(Role::name).orElse(null),
				AuditContext.currentIpAddress()
		);

		return new MessageResponse("Password changed successfully.");
	}

	private void sendResetEmail(User user) {
		passwordResetTokenRepository.invalidateActiveTokensForUser(user);

		String tokenValue = UUID.randomUUID().toString();
		PasswordResetToken resetToken = PasswordResetToken.builder()
				.user(user)
				.token(tokenValue)
				.expiresAt(Instant.now().plus(passwordResetProperties.expirationMinutes(), ChronoUnit.MINUTES))
				.build();
		passwordResetTokenRepository.save(resetToken);

		String resetLink = buildResetLink(tokenValue);
		String subject = "Reset your Utility Billing System password";
		String body = """
				Hello %s,

				We received a request to reset your password.

				Use this link to set a new password (valid for %d minutes):
				%s

				Or use this reset token in the API:
				%s

				If you did not request this, you can ignore this email.

				—
				%s
				""".formatted(
				user.getUsername(),
				passwordResetProperties.expirationMinutes(),
				resetLink,
				tokenValue,
				mailProperties.fromName()
		);

		emailService.sendPlainText(user.getEmail(), subject, body);
	}

	private String buildResetLink(String token) {
		String baseUrl = passwordResetProperties.baseUrl();
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl + "?token=" + token;
	}

}
