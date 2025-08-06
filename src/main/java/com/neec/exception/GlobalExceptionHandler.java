package com.neec.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(exception = {UserAlreadyExistsException.class})
	public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex){
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("registration_status", ex.getMessage()));
	}
	
	@ExceptionHandler(exception = {HttpMessageNotReadableException.class})
	public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Request_Body_Error", "Please check request body"));
	}
}
