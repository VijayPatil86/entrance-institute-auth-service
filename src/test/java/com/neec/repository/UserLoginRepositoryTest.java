package com.neec.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.neec.entity.UserLogin;
import com.neec.enums.EnumRole;
import com.neec.enums.EnumUserAccountStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserLoginRepositoryTest {
	@Autowired
	private UserLoginRepository userLoginRepository;

	@Test
	void test_findByEmailAddress_Returns_User_When_EmailExists() {
		String strUID = UUID.randomUUID().toString();
		UserLogin newUser = UserLogin.builder()
				.emailAddress("test@gmail.com")
				.hashedPassword("hashed_password")
				.accountStatus(EnumUserAccountStatus.PENDING_VERIFICATION)
				.verificationToken(strUID)
				.verificationTokenExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(24))
				.role(EnumRole.APPLICANT)
				.build();
		userLoginRepository.save(newUser);
		Optional<UserLogin> optUser = userLoginRepository.findByEmailAddress("test@gmail.com");
		assertTrue(optUser.isPresent(), "Expected: Record must be fetched for existing email");
		UserLogin user = optUser.get();
		assertEquals("test@gmail.com", user.getEmailAddress());
		assertEquals(EnumUserAccountStatus.PENDING_VERIFICATION, user.getAccountStatus());
		assertEquals(strUID, user.getVerificationToken());
	}

	@Test
	void test_findByEmailAddress_Returns_Nothing_When_EmailNotExists() {
		// when this test starts, database is empty meaning there is no record
		Optional<UserLogin> optUser = userLoginRepository.findByEmailAddress("test@gmail.com");
		assertTrue(optUser.isEmpty());
	}

	@Test
	void test_findByVerificationToken_Returns_Nothing_When_TokenNotExists() {
		Optional<UserLogin> optUser = userLoginRepository.findByVerificationToken("non-existing-token");
		assertTrue(optUser.isEmpty());
	}

	@Test
	void test_findByVerificationToken_Returns_UserLogin_When_ValidToken() {
		String strUID = UUID.randomUUID().toString();
		UserLogin newUser = UserLogin.builder()
				.emailAddress("test@gmail.com")
				.hashedPassword("hashed_password")
				.accountStatus(EnumUserAccountStatus.PENDING_VERIFICATION)
				.verificationToken(strUID)
				.verificationTokenExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(24))
				.role(EnumRole.APPLICANT)
				.build();
		userLoginRepository.save(newUser);
		Optional<UserLogin> optUser = userLoginRepository.findByVerificationToken(strUID);
		assertTrue(optUser.isPresent());
		assertTrue(optUser.get().getVerificationToken().equals(strUID));
	}
}
