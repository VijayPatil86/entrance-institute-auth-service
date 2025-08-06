package com.neec.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neec.dto.LoginRequestDTO;
import com.neec.dto.RegistrationRequestDTO;
import com.neec.entity.UserLogin;
import com.neec.enums.EnumUserAccountStatus;
import com.neec.exception.UserAccountSuspendedException;
import com.neec.exception.UserAlreadyExistsException;
import com.neec.exception.UserNotFoundException;
import com.neec.exception.UserNotVerifiedException;
import com.neec.repository.UserLoginRepository;

import io.micrometer.observation.annotation.Observed;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
	private UserLoginRepository userLoginRepository;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public AuthenticationServiceImpl(UserLoginRepository userLoginRepository, 
			BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.userLoginRepository = userLoginRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Observed(name = "authentication.service.register.user", contextualName = "registering a new user")
	@Override
	public void registerUser(RegistrationRequestDTO dto) {
		String emailAddress = dto.getEmailAddress();
		Optional<UserLogin> optExistingUser = userLoginRepository.findByEmailAddress(emailAddress);
		if(optExistingUser.isPresent()) {
			throw new UserAlreadyExistsException("Email is already used.");
		}
		UserLogin newUser = UserLogin.builder()
				.emailAddress(emailAddress.trim())
				.hashedPassword(bCryptPasswordEncoder.encode(dto.getPassword()))
				.accountStatus(EnumUserAccountStatus.PENDING_VERIFICATION)
				.verificationToken(UUID.randomUUID().toString())
				.verificationTokenExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(24))
				.build();
		UserLogin savedUser = userLoginRepository.save(newUser);
	}

	@Transactional(readOnly = true)
	@Observed(name = "authentication.service.login.user", contextualName = "user login")
	@Override
	public void login(LoginRequestDTO loginRequestDTO) {
		String emailAddress = loginRequestDTO.getEmailAddress().trim();
		UserLogin userLogin = userLoginRepository.findByEmailAddress(emailAddress)
				.orElseThrow(() -> new UserNotFoundException("invalid email or password."));
		if(userLogin.getAccountStatus().equals(EnumUserAccountStatus.SUSPENDED)) {
			throw new UserAccountSuspendedException("Your account is suspended. Please contact Administrator");
		}
		if(userLogin.getAccountStatus().equals(EnumUserAccountStatus.PENDING_VERIFICATION)) {
			throw new UserNotVerifiedException("Your account is not verified. Please check your email.");
		}
		if(!bCryptPasswordEncoder.matches(loginRequestDTO.getPassword(), userLogin.getHashedPassword())) {
			throw new UserNotFoundException("invalid email or password.");
		}
	}
}
