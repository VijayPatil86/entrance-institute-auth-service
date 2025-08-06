package com.neec.service;

import com.neec.dto.LoginRequestDTO;
import com.neec.dto.RegistrationRequestDTO;
import com.neec.exception.UserNotVerifiedException;

public interface AuthenticationService {
	/**
	 * Registers a new user in the system
	 * 
	 * @param registrationRequest The DTO containing the user's email and password.
	 * @throws UserAlreadyExistsException if an account with the given email already exists.
	 */
	void registerUser(RegistrationRequestDTO dto);
	
	/**
	 * method to login user. On successful login, JWT token is returned
	 * @param loginRequestDTO
	 * @return a JWT token
	 * @throws UserNotFoundException if email or password is not found
	 * @throws UserAccountSuspendedException when the user account is suspended
	 * @throws UserNotVerifiedException when user account verification is in Pending stage
	 */
	String login(LoginRequestDTO loginRequestDTO);
}
