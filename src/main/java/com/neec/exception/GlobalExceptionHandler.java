package com.neec.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	// MethodArgumentNotValidException when bean field validation fails, e.g. @NotBlank, @Pattern
	@ExceptionHandler(exception = {MethodArgumentNotValidException.class})
	public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
						(existing, replacement) -> existing));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(exception = {MissingServletRequestParameterException.class})
	public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Request parameter 'token' is required."));
	}

	@ExceptionHandler(exception = {ConstraintViolationException.class})
	public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Verification token can not be empty."));
	}

	@ExceptionHandler(exception = {UserAlreadyExistsException.class})
	public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex){
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("registration_status", ex.getMessage()));
	}

	@ExceptionHandler(exception = {HttpMessageNotReadableException.class})
	public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Request_Body_Error", "Please check request body"));
	}

	@ExceptionHandler(exception = {UserNotFoundException.class, UserAccountSuspendedException.class,
			UserNotVerifiedException.class})
	public ResponseEntity<Map<String, String>> handleAuthenticationExceptions(RuntimeException ex){
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(exception = {InvalidTokenException.class})
	public ResponseEntity<Map<String, String>> handleInvalidTokenException(InvalidTokenException ex){
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
	}
}
