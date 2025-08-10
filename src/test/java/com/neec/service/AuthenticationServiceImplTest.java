package com.neec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.neec.dto.LoginRequestDTO;
import com.neec.dto.RegistrationRequestDTO;
import com.neec.entity.UserLogin;
import com.neec.enums.EnumRole;
import com.neec.enums.EnumUserAccountStatus;
import com.neec.exception.UserAccountSuspendedException;
import com.neec.exception.UserAlreadyExistsException;
import com.neec.exception.UserNotFoundException;
import com.neec.exception.UserNotVerifiedException;
import com.neec.repository.UserLoginRepository;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
	@Mock
	private UserLoginRepository mockUserLoginRepository;
	@Mock
	private BCryptPasswordEncoder mockBCryptPasswordEncoder;
	@Mock
	private RabbitTemplate mockRabbitTemplate;
	@Mock
	private JwtService mockJwtService;
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
				.verificationToken("7775c963-1d25-4f0b-b89d-b1b1d88cc4cb")
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

	@Test
	void test_registerUser_NewEmailAddress_Save_User_SendEmail() {
		ReflectionTestUtils.setField(authenticationServiceImpl, "topicExchangeName", "email_exchange");
		ReflectionTestUtils.setField(authenticationServiceImpl, "routingKey", "routing.key");

		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("new.email.address@gmail.com")
				.password("P@$$w0rd")
				.build();
		when(mockUserLoginRepository.findByEmailAddress("new.email.address@gmail.com"))
			.thenReturn(Optional.empty());
		when(mockBCryptPasswordEncoder.encode(dto.getPassword()))
			.thenReturn("hashed-password");
		UserLogin mockSavedUser = UserLogin.builder()
				.emailAddress("new.email.address@gmail.com")
				.verificationToken("7775c963-1d25-4f0b-b89d-b1b1d88cc4cb")
				.build();
		when(mockUserLoginRepository.save(any(UserLogin.class))).thenReturn(mockSavedUser);

		authenticationServiceImpl.registerUser(dto);

		verify(mockUserLoginRepository).findByEmailAddress("new.email.address@gmail.com");
		verify(mockUserLoginRepository, times(1)).save(any(UserLogin.class));

		ArgumentCaptor<String> emailExchangeNameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
		verify(mockRabbitTemplate).convertAndSend(emailExchangeNameCaptor.capture(),
				routingKeyCaptor.capture(), messageCaptor.capture());
		assertEquals("email_exchange", emailExchangeNameCaptor.getValue(), "Expected: correct Exchange name must be set");
		assertEquals("routing.key", routingKeyCaptor.getValue(), "Expected: correct message routing key must be set");
		Map<String, String> messageSent = messageCaptor.getValue();
		assertEquals("new.email.address@gmail.com", messageSent.get("email"), "Expected: correct email must be set");
		assertEquals("7775c963-1d25-4f0b-b89d-b1b1d88cc4cb", messageSent.get("token"), "Expected: correct token be set");
	}

	@Test
	void testLogin_EmailAddressNotAvailable_RaiseUserNotFoundException() {
		LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
				.emailAddress("unavailable.email.address@gmail.com")
				.build();
		when(mockUserLoginRepository.findByEmailAddress("unavailable.email.address@gmail.com"))
			.thenReturn(Optional.empty());
		UserNotFoundException ex = assertThrows(UserNotFoundException.class,
				() -> authenticationServiceImpl.login(loginRequestDTO));
		verify(mockUserLoginRepository).findByEmailAddress("unavailable.email.address@gmail.com");
		assertEquals("invalid email or password.", ex.getMessage());
	}

	@Test
	void testLogin_EmailAddressAvailable_InvalidPassword_RaiseUserNotFoundException() {
		LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
				.emailAddress("available.email.address@gmail.com")
				.password("invalid-password")
				.build();
		Optional<UserLogin> optUserLogin =
				Optional.of(
						UserLogin.builder()
						.hashedPassword("hashed-password")
						.build()
				);
		when(mockUserLoginRepository.findByEmailAddress(any(String.class)))
			.thenReturn(optUserLogin);
		when(mockBCryptPasswordEncoder.matches(any(String.class), any(String.class)))
			.thenReturn(false);
		UserNotFoundException ex =
				assertThrows(UserNotFoundException.class,
						() -> authenticationServiceImpl.login(loginRequestDTO));
		verify(mockUserLoginRepository).findByEmailAddress(any(String.class));
		verify(mockBCryptPasswordEncoder).matches(any(String.class), any(String.class));
		assertEquals("invalid email or password.", ex.getMessage());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountIsSuspended() {
		LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
				.emailAddress("available.email.address@gmail.com")
				.password("valid-password")
				.build();
		Optional<UserLogin> optUserLogin =
				Optional.of(
						UserLogin.builder()
						.hashedPassword("hashed-password")
						.accountStatus(EnumUserAccountStatus.SUSPENDED)
						.build()
				);
		when(mockUserLoginRepository.findByEmailAddress(any(String.class)))
			.thenReturn(optUserLogin);
		when(mockBCryptPasswordEncoder.matches(any(String.class), any(String.class)))
			.thenReturn(true);
		UserAccountSuspendedException ex =
				assertThrows(UserAccountSuspendedException.class,
						() -> authenticationServiceImpl.login(loginRequestDTO));
		verify(mockUserLoginRepository).findByEmailAddress(any(String.class));
		verify(mockBCryptPasswordEncoder).matches(any(String.class), any(String.class));
		assertEquals("Your account is suspended. Please contact Administrator",
				ex.getMessage());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountPendingVerification() {
		LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
				.emailAddress("available.email.address@gmail.com")
				.password("valid-password")
				.build();
		Optional<UserLogin> optUserLogin =
				Optional.of(
						UserLogin.builder()
						.hashedPassword("hashed-password")
						.accountStatus(EnumUserAccountStatus.PENDING_VERIFICATION)
						.build()
				);
		when(mockUserLoginRepository.findByEmailAddress(any(String.class)))
			.thenReturn(optUserLogin);
		when(mockBCryptPasswordEncoder.matches(any(String.class), any(String.class)))
			.thenReturn(true);
		UserNotVerifiedException ex =
				assertThrows(UserNotVerifiedException.class,
						() -> authenticationServiceImpl.login(loginRequestDTO));
		verify(mockUserLoginRepository).findByEmailAddress(any(String.class));
		verify(mockBCryptPasswordEncoder).matches(any(String.class), any(String.class));
		assertEquals("Your account is not verified. Please check your email.",
				ex.getMessage());
	}

	@Test
	void testLogin_TestJwtGeneration() {
		LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
				.emailAddress("available.email.address@gmail.com")
				.password("valid-password")
				.build();
		Optional<UserLogin> optUserLogin =
				Optional.of(
						UserLogin.builder()
						.userLoginId(1L)
						.emailAddress("available.email.address@gmail.com")
						.hashedPassword("hashed-password")
						.accountStatus(EnumUserAccountStatus.ACTIVE)
						.role(EnumRole.APPLICANT)
						.build()
				);
		when(mockUserLoginRepository.findByEmailAddress(any(String.class)))
			.thenReturn(optUserLogin);
		when(mockBCryptPasswordEncoder.matches(any(String.class), any(String.class)))
			.thenReturn(true);
		String mockJwtToken = "mock-jwt-token";
		when(mockJwtService.generateJwtToken(anyLong(), anyString(), anyString()))
			.thenReturn(mockJwtToken);
		String jwtToken = authenticationServiceImpl.login(loginRequestDTO);
		verify(mockUserLoginRepository).findByEmailAddress(any(String.class));
		verify(mockBCryptPasswordEncoder).matches(any(String.class), any(String.class));

		ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<String> emailAddressCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> userRoleCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockJwtService).generateJwtToken(userIdCaptor.capture(),
				emailAddressCaptor.capture(), userRoleCaptor.capture());
		assertEquals(1L, userIdCaptor.getValue());
		assertEquals("available.email.address@gmail.com", emailAddressCaptor.getValue());
		assertEquals(EnumRole.APPLICANT.name(), userRoleCaptor.getValue());
		assertEquals("mock-jwt-token", jwtToken);
	}
}
