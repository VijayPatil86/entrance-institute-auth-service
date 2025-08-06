package com.neec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.neec.dto.RegistrationRequestDTO;
import com.neec.entity.UserLogin;
import com.neec.enums.EnumUserAccountStatus;
import com.neec.exception.UserAlreadyExistsException;
import com.neec.repository.UserLoginRepository;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
	@Mock
	private UserLoginRepository mockUserLoginRepository;
	@Mock
	private BCryptPasswordEncoder mockBCryptPasswordEncoder;
	@InjectMocks
	private AuthenticationServiceImpl authenticationServiceImpl;
	
	@Test
	void test_registerUser_ExistingEmailAddress_Raise_UserAlreadyExistsException() {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("existing.email.address@gmail.com")
				.password("P@$$w0rd")
				.build();
		Optional<UserLogin> optMockUserLogin = Optional.of(UserLogin.builder().build());
		when(mockUserLoginRepository.findByEmailAddress("existing.email.address@gmail.com"))
			.thenReturn(optMockUserLogin);
		UserAlreadyExistsException userAlreadyExistsException =
				assertThrows(UserAlreadyExistsException.class, 
						() -> authenticationServiceImpl.registerUser(dto));
		verify(mockUserLoginRepository).findByEmailAddress("existing.email.address@gmail.com");
		verify(mockUserLoginRepository, never()).save(any(UserLogin.class));
		assertEquals("Email is already used.", userAlreadyExistsException.getMessage(), 
				"Expected: Exception message must be: Email is already used.");
	}
	
	@Test
	void test_registerUser_NewEmailAddress_Save_User() {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("new.email.address@gmail.com")
				.password("P@$$w0rd")
				.build();
		UserLogin mockSavedUser = UserLogin.builder()
				.userLoginId(1L)
				.emailAddress("new.email.address@gmail.com")
				.accountStatus(EnumUserAccountStatus.ACTIVE)
				.build();
		when(mockUserLoginRepository.findByEmailAddress("new.email.address@gmail.com"))
			.thenReturn(Optional.empty());
		when(mockBCryptPasswordEncoder.encode(dto.getPassword()))
			.thenReturn("hashed-password");
		when(mockUserLoginRepository.save(any(UserLogin.class))).thenReturn(mockSavedUser);
		
		authenticationServiceImpl.registerUser(dto);
		
		verify(mockUserLoginRepository).findByEmailAddress("new.email.address@gmail.com");
		verify(mockBCryptPasswordEncoder).encode(dto.getPassword());
		
		ArgumentCaptor<UserLogin> userLoginCaptor = ArgumentCaptor.forClass(UserLogin.class);
		verify(mockUserLoginRepository, times(1)).save(userLoginCaptor.capture());
		UserLogin tobeSavedUserLogin = userLoginCaptor.getValue();
		assertEquals("new.email.address@gmail.com", tobeSavedUserLogin.getEmailAddress(),
				"Expected: correct email address must be set");
		assertEquals("hashed-password", tobeSavedUserLogin.getHashedPassword(),
				"Expected: password must be hashed");
		assertEquals(EnumUserAccountStatus.PENDING_VERIFICATION, tobeSavedUserLogin.getAccountStatus());
		assertNotNull(tobeSavedUserLogin.getVerificationToken(), "Expected: verification token must be set");
		assertNotNull(tobeSavedUserLogin.getVerificationTokenExpiresAt(), "Expected: verification token expiring datetime must be set");
	}
}
