package com.neec.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class LoginRequestDTOTest {
	private Validator validator;
	
	@BeforeEach
	void setup() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "  ", "a#gmail.com", "plainaddress", "@missingusername.com"})
	void test_emailAddress_Invalid(String input) {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress(input)
				.password("P@$$w0rd")
				.build();
		boolean hasEmailAddressViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("emailAddress"));
		assertTrue(hasEmailAddressViolation, "Expected: email address validation error for email format: " + input);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"test@example.com", "test.name+alias@gmail.com"})
	void test_emailAddress_Valid(String input) {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress(input)
				.password("P@$$w0rd")
				.build();
		boolean hasEmailAddressViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("emailAddress"));
		assertFalse(hasEmailAddressViolation, "Validation failed for a valid email address: " + input);
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {""})
	void test_password_invalid(String input) {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("test@example.com")
				.password(input)
				.build();
		boolean hasPasswordViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
		assertTrue(hasPasswordViolation, "Expected: password validation error for input: " + input);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"pasword123", "P@$$w0rd"})
	void test_password_Valid(String input) {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("test@example.com")
				.password(input)
				.build();
		boolean hasPasswordViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
		assertFalse(hasPasswordViolation, "Validation failed for password, input: " + input);
	}
	
}
