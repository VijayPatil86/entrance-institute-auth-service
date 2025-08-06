package com.neec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE) @Getter @Setter
public class LoginRequestDTO {
	@Schema(description = "email address to login", example = "john.doe@gmail.com")
	@NotBlank(message = "Email address cannot be blank.")
	@Email(message = "Please provide a valid email address.")
	@Pattern(message = "Please provide a valid email address.", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
	private String emailAddress;
	
	@Schema(description = "Password to login", example = "P@$$w0rd")
	@NotBlank(message = "Password cannot be blank.")
	private String password;
}
