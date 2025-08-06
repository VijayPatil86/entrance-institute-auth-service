package com.neec.service;

import com.neec.dto.LoginRequestDTO;
import com.neec.dto.RegistrationRequestDTO;

public interface AuthenticationService {
	/**
	 * Registers a new user in the system
	 * 
	 * @param registrationRequest The DTO containing the user's email and password.
	 * @throws UserAlreadyExistsException if an account with the given email already exists.
	 */
	void registerUser(RegistrationRequestDTO dto);
	
	void login(LoginRequestDTO loginRequestDTO);
}
