package com.neec.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neec.entity.UserLogin;

public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
	Optional<UserLogin> findByEmailAddress(String emailAddress);
	Optional<UserLogin> findByVerificationToken(String verificationToken);
}
