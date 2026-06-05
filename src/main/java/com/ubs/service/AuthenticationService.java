package com.ubs.service;

import com.ubs.audit.AuditContext;
import com.ubs.dto.auth.AuthResponse;
import com.ubs.dto.auth.LoginRequest;
import com.ubs.dto.auth.OtpChallengeResponse;
import com.ubs.dto.auth.RefreshTokenRequest;
import com.ubs.dto.auth.RegisterRequest;
import com.ubs.dto.auth.UserResponse;
import com.ubs.dto.auth.VerifyOtpRequest;
import com.ubs.entity.AuditAction;
import com.ubs.entity.Customer;
import com.ubs.entity.CustomerStatus;
import com.ubs.entity.OtpChallenge;
import com.ubs.entity.OtpPurpose;
import com.ubs.entity.Role;
import com.ubs.entity.User;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.repository.CustomerRepository;
import com.ubs.repository.UserRepository;
import com.ubs.security.CustomUserDetails;
import com.ubs.security.InvalidTokenException;
import com.ubs.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthenticationService {

	private static final String BEARER_TOKEN_TYPE = "Bearer";

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final AuditLogService auditLogService;
	private final OtpService otpService;

	public AuthenticationService(UserRepository userRepository,
								 CustomerRepository customerRepository,
								 PasswordEncoder passwordEncoder,
								 AuthenticationManager authenticationManager,
								 JwtService jwtService,
								 AuditLogService auditLogService,
								 OtpService otpService) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.auditLogService = auditLogService;
		this.otpService = otpService;
	}

	@Transactional
	public OtpChallengeResponse register(RegisterRequest request) {
		validateRegistrationUniqueness(request);
		return otpService.initiateRegister(request);
	}

	@Transactional
	public AuthResponse verifyRegisterOtp(VerifyOtpRequest request) {
		OtpChallenge challenge = otpService.verify(request, OtpPurpose.REGISTER);
		RegisterRequest registerRequest = otpService.deserializeRegisterPayload(challenge);
		validateRegistrationUniqueness(registerRequest);

		AuthResponse response = completeRegistration(registerRequest);
		otpService.markUsed(challenge);
		return response;
	}

	@Transactional
	public OtpChallengeResponse login(LoginRequest request) {
		User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail())
				.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(user.getUsername(), request.password()));

		return otpService.initiateLogin(user);
	}

	@Transactional
	public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
		OtpChallenge challenge = otpService.verify(request, OtpPurpose.LOGIN);

		User user = challenge.getUser();
		if (user == null) {
			throw new BadCredentialsException("Invalid OTP session");
		}

		CustomUserDetails userDetails = new CustomUserDetails(user);

		auditLogService.log(
				AuditAction.USER_LOGIN,
				"User",
				userDetails.getId(),
				"User logged in: " + userDetails.getUsername(),
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getRoles().stream().findFirst().map(Role::name).orElse(null),
				AuditContext.currentIpAddress()
		);

		otpService.markUsed(challenge);
		return buildAuthResponse(userDetails);
	}

	@Transactional(readOnly = true)
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		String refreshToken = request.refreshToken();
		String username = jwtService.extractUsername(refreshToken);

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

		CustomUserDetails userDetails = new CustomUserDetails(user);

		if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
			throw new InvalidTokenException("Invalid or expired refresh token");
		}

		return buildAuthResponse(userDetails);
	}

	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(CustomUserDetails userDetails) {
		return toUserResponse(userDetails);
	}

	private AuthResponse completeRegistration(RegisterRequest request) {
		Customer customer = Customer.builder()
				.fullNames(request.fullNames().trim())
				.nationalId(request.nationalId().trim())
				.email(request.email().trim().toLowerCase())
				.phone(request.phone().trim())
				.address(request.address().trim())
				.status(CustomerStatus.ACTIVE)
				.build();
		Customer savedCustomer = customerRepository.save(customer);

		User user = User.builder()
				.username(request.username().trim())
				.email(request.email().trim().toLowerCase())
				.password(passwordEncoder.encode(request.password()))
				.roles(Set.of(Role.ROLE_CUSTOMER))
				.customer(savedCustomer)
				.build();

		User savedUser = userRepository.save(user);
		CustomUserDetails userDetails = new CustomUserDetails(savedUser);

		auditLogService.log(
				AuditAction.USER_REGISTER,
				"User",
				savedUser.getId(),
				"Customer registered: " + savedUser.getUsername(),
				savedUser.getId(),
				savedUser.getUsername(),
				Role.ROLE_CUSTOMER.name(),
				AuditContext.currentIpAddress()
		);

		return buildAuthResponse(userDetails);
	}

	private void validateRegistrationUniqueness(RegisterRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new DuplicateResourceException("Username is already taken");
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateResourceException("Email is already registered");
		}
		if (customerRepository.existsByNationalId(request.nationalId().trim())) {
			throw new DuplicateResourceException("National ID is already registered");
		}
	}

	private AuthResponse buildAuthResponse(CustomUserDetails userDetails) {
		String accessToken = jwtService.generateAccessToken(userDetails);
		String refreshToken = jwtService.generateRefreshToken(userDetails);

		return new AuthResponse(
				accessToken,
				refreshToken,
				BEARER_TOKEN_TYPE,
				jwtService.getAccessTokenExpirationMs(),
				toUserResponse(userDetails)
		);
	}

	private UserResponse toUserResponse(CustomUserDetails userDetails) {
		return new UserResponse(
				userDetails.getId(),
				userDetails.getCustomerId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				userDetails.getRoles()
		);
	}

}
