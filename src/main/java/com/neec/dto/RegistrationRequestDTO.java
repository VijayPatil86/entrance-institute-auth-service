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

@Schema(description = "Registration Request DTO")
@Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE) @Getter @Setter
public class RegistrationRequestDTO {
	@Schema(description = "Email address to register", example = "john.doe@gmail.com")
	@NotBlank(message = "Email address cannot be blank.")
	@Email(message = "Please provide a valid email address.")
	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,6}$", 
		message = "Please provide a valid email address format.")
	private String emailAddress;
	
	@Schema(description = "valid password", example = "P@@sw0rd1")
	@NotBlank(message = "Password cannot be blank.")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\\$%^&+=!])(?=\\S+$).{8,}$",
			message = "Password must be at least 8 characters long and contain at least one digit, "
					+ "one lowercase letter, one uppercase letter, and one special character.")
	private String password;
}
