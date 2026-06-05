package com.ubs.exception;

import com.ubs.dto.ErrorResponse;
import com.ubs.security.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
														  HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining("; "));

		return buildError(HttpStatus.BAD_REQUEST, "Validation Failed", message, request);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
														 HttpServletRequest request) {
		return buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
														HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
	}

	@ExceptionHandler(BusinessRuleViolationException.class)
	public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleViolationException ex,
														  HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
															 HttpServletRequest request) {
		String message = "A database constraint was violated";
		if (ex.getMessage() != null) {
			String details = ex.getMessage().toLowerCase();
			if (details.contains("national_id")) {
				message = "National ID is already registered";
			} else if (details.contains("meter_number")) {
				message = "Meter number is already registered";
			} else if (details.contains("uk_meter_readings_meter_period")
					|| details.contains("reading_month")) {
				message = "A reading already exists for this meter in the given month and year";
			} else if (details.contains("uk_bills_meter_reading")) {
				message = "A bill already exists for this meter reading";
			} else if (details.contains("uk_bills_meter_period")
					|| details.contains("billing_month")) {
				message = "A bill already exists for this meter in the given month and year";
			} else if (details.contains("bill_number")) {
				message = "Bill number already exists";
			} else if (details.contains("payment_reference")) {
				message = "Payment reference already exists";
			}
		}
		return buildError(HttpStatus.CONFLICT, "Conflict", message, request);
	}

	@ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
	public ResponseEntity<ErrorResponse> handleBadCredentials(RuntimeException ex,
															  HttpServletRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
															HttpServletRequest request) {
		return buildError(HttpStatus.FORBIDDEN, "Forbidden",
				"You do not have permission to access this resource.", request);
	}

	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<ErrorResponse> handlePropertyReference(PropertyReferenceException ex,
																 HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Bad Request",
				"Invalid sort or filter property: " + ex.getPropertyName(), request);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex,
															HttpServletRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
				"An unexpected error occurred", request);
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}

	private ResponseEntity<ErrorResponse> buildError(HttpStatus status,
													 String error,
													 String message,
													 HttpServletRequest request) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				error,
				message,
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}

}
