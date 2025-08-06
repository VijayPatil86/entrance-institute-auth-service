package com.neec.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neec.dto.RegistrationRequestDTO;
import com.neec.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
	private AuthenticationService authenticationService;
	
	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	@Tag(name = "Registration", description = "Performs Registration")
	@Operation(
			summary = "Registration using valid email address, password",
			responses = {
					@ApiResponse(responseCode = "201", description = "Registration complete"),
					@ApiResponse(responseCode = "400", description = "invalid email address or password"),
					@ApiResponse(responseCode = "409", description = "Existing email address entered")
			}
	)
	@PostMapping(path = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequestDTO dto,
			BindingResult validationResult) {
		if(validationResult.hasFieldErrors()) { 
			Map<String, String> errors = validationResult.getFieldErrors().stream()
					.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
							(existing, replacement) -> existing));
			return ResponseEntity.badRequest().body(errors);
		}
		authenticationService.registerUser(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("registration_status", "User registered successfully."));
	}
}
