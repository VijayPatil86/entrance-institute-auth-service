package com.neec.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class RegistrationRequestDTOTest {
	private Validator validator;
	
	@BeforeEach
	void setup() {
		this.validator = Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "  ", "a#gmail.com", "plainaddress", "@missingusername.com", "abc@gmail#com"})
	void test_emailAddress_Invalid(String input) {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress(input)
				.password("P@$$w0rd")
				.build();
		boolean hasEmailAddressViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("emailAddress"));
		assertTrue(hasEmailAddressViolation, "Expected: email address validation error for email format: " + input);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"test@example.com", "test.name+alias@gmail.com", "existing.email.address@gmail.com"})
	void test_emailAddress_Valid(String input) {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress(input)
				.password("P@$$w0rd")
				.build();
		boolean hasEmailAddressViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("emailAddress"));
		assertFalse(hasEmailAddressViolation, "Validation failed for a valid email address: " + input);
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "        ", "short", "nouppercase@1", "NOLOWERCASEP@$$W@@RD1", 
			"NoSpecialChar1", "NoDigit@Char", "P@ssword 123", "Password", "pasword123",
			"PASSWORD1", "Pass@", "12345678", "P@sswrd"
			})
	void test_password_invalid(String input) {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("test@example.com")
				.password(input)
				.build();
		boolean hasPasswordViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
		assertTrue(hasPasswordViolation, "Expected: password validation error for input: " + input);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"P@$$w0rd", "P@ssw0rd123", "Secure!Pass9", "Another&Good1"})
	void test_password_Valid(String input) {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("test@example.com")
				.password(input)
				.build();
		boolean hasPasswordViolation = validator.validate(dto).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
		assertFalse(hasPasswordViolation, "Validation failed for password, input: " + input);
	}
}
