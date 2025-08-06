package com.neec.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neec.dto.LoginRequestDTO;
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
	public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequestDTO dto) {
		authenticationService.registerUser(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("registration_status", "User registered successfully."));
	}

	@Tag(name = "Login", description = "Performs User Login")
	@Operation(
			summary = "Authenticates a user and returns a JWT",
			responses = {
					@ApiResponse(responseCode = "200", description = "Login successful, JWT returned"),
					@ApiResponse(responseCode = "400", description = "Invalid request body"),
					@ApiResponse(responseCode = "401",
						description = "Invalid credentials, user not verified, or account suspended")
			}
	)
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto) {
		String jwtToken = authenticationService.login(dto);
		return ResponseEntity.ok(Map.of("token", jwtToken));
	}
}
