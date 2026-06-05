package com.ubs.controller;

import com.ubs.config.openapi.OpenApiConstants;
import com.ubs.config.openapi.OpenApiExamples;
import com.ubs.dto.auth.AuthResponse;
import com.ubs.dto.auth.ChangePasswordRequest;
import com.ubs.dto.auth.ForgotPasswordRequest;
import com.ubs.dto.auth.LoginRequest;
import com.ubs.dto.auth.MessageResponse;
import com.ubs.dto.auth.OtpChallengeResponse;
import com.ubs.dto.auth.RefreshTokenRequest;
import com.ubs.dto.auth.RegisterRequest;
import com.ubs.dto.auth.ResetPasswordRequest;
import com.ubs.dto.auth.UserResponse;
import com.ubs.dto.auth.VerifyOtpRequest;
import com.ubs.security.CustomUserDetails;
import com.ubs.service.AuthenticationService;
import com.ubs.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration, login, token refresh, and profile")
public class AuthController {

	private final AuthenticationService authenticationService;
	private final PasswordResetService passwordResetService;

	public AuthController(AuthenticationService authenticationService,
						  PasswordResetService passwordResetService) {
		this.authenticationService = authenticationService;
		this.passwordResetService = passwordResetService;
	}

	@PostMapping("/register")
	@SecurityRequirements
	@Operation(
			summary = "Register a new customer account (step 1 — send OTP)",
			description = """
					Validates registration details and sends a 6-digit OTP to the user's email.
					Complete registration with POST /auth/register/verify-otp.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "OTP sent — verify to complete registration",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = OtpChallengeResponse.class),
							examples = @ExampleObject(name = "OtpChallengeResponse", value = OpenApiExamples.OTP_CHALLENGE_RESPONSE)
					)
			),
			@ApiResponse(
					responseCode = "400",
					description = "Validation failed",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							examples = @ExampleObject(name = "ValidationError", value = OpenApiExamples.ERROR_RESPONSE)
					)
			),
			@ApiResponse(responseCode = "409", description = "Username or email already exists")
	})
	public ResponseEntity<OtpChallengeResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authenticationService.register(request));
	}

	@PostMapping("/register/verify-otp")
	@SecurityRequirements
	@Operation(
			summary = "Register a new customer account (step 2 — verify OTP)",
			description = "Verifies the OTP and creates the account. Returns JWT access and refresh tokens."
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "Account created successfully",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = AuthResponse.class),
							examples = @ExampleObject(name = "AuthResponse", value = OpenApiExamples.AUTH_RESPONSE)
					)
			),
			@ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
	})
	public ResponseEntity<AuthResponse> verifyRegisterOtp(@Valid @RequestBody VerifyOtpRequest request) {
		AuthResponse response = authenticationService.verifyRegisterOtp(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(
			summary = "Authenticate (step 1 — send OTP)",
			description = """
					Validates username/email and password, then sends a 6-digit OTP to the user's email.
					Complete login with POST /auth/login/verify-otp to obtain JWT tokens.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "OTP sent — verify to complete login",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = OtpChallengeResponse.class),
							examples = @ExampleObject(name = "OtpChallengeResponse", value = OpenApiExamples.OTP_CHALLENGE_RESPONSE)
					)
			),
			@ApiResponse(responseCode = "401", description = "Invalid credentials")
	})
	public ResponseEntity<OtpChallengeResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authenticationService.login(request));
	}

	@PostMapping("/login/verify-otp")
	@SecurityRequirements
	@Operation(
			summary = "Authenticate (step 2 — verify OTP)",
			description = """
					Verifies the OTP and returns JWT tokens.
					Use accessToken as: Authorization: Bearer {accessToken}
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Authentication successful",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = AuthResponse.class),
							examples = @ExampleObject(name = "AuthResponse", value = OpenApiExamples.AUTH_RESPONSE)
					)
			),
			@ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
	})
	public ResponseEntity<AuthResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
		return ResponseEntity.ok(authenticationService.verifyLoginOtp(request));
	}

	@PostMapping("/refresh")
	@SecurityRequirements
	@Operation(
			summary = "Obtain a new access token using a refresh token",
			description = "Exchange a valid refresh token for a new access/refresh token pair."
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Token refreshed successfully",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = AuthResponse.class),
							examples = @ExampleObject(name = "AuthResponse", value = OpenApiExamples.AUTH_RESPONSE)
					)
			),
			@ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
	})
	public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(authenticationService.refreshToken(request));
	}

	@GetMapping("/me")
	@Operation(
			summary = "Get the currently authenticated user",
			description = "Returns profile details for the user associated with the JWT access token."
	)
	@SecurityRequirement(name = OpenApiConstants.BEARER_AUTH_SCHEME)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Current user profile",
					content = @Content(schema = @Schema(implementation = UserResponse.class))
			),
			@ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
	})
	public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(authenticationService.getCurrentUser(userDetails));
	}

	@PostMapping("/forgot-password")
	@SecurityRequirements
	@Operation(
			summary = "Request a password reset email",
			description = "Sends a reset link and token to the user's email if the account exists."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Request accepted"),
			@ApiResponse(responseCode = "400", description = "Validation failed")
	})
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return ResponseEntity.ok(passwordResetService.forgotPassword(request));
	}

	@PostMapping("/reset-password")
	@SecurityRequirements
	@Operation(
			summary = "Reset password using email token",
			description = "Sets a new password using the token received by email."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Password reset successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token")
	})
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		return ResponseEntity.ok(passwordResetService.resetPassword(request));
	}

	@PostMapping("/change-password")
	@Operation(
			summary = "Change password for the logged-in user",
			description = "Requires the current password and a new password."
	)
	@SecurityRequirement(name = OpenApiConstants.BEARER_AUTH_SCHEME)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Password changed successfully"),
			@ApiResponse(responseCode = "400", description = "Current password is incorrect"),
			@ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
	})
	public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
														  @Valid @RequestBody ChangePasswordRequest request) {
		return ResponseEntity.ok(passwordResetService.changePassword(userDetails, request));
	}

}
