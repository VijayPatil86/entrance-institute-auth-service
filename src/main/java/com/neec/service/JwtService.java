package com.neec.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
	@Value("${jwt.secret.text}")
	private String jwtSecretText;

	@Value("${jwt.token.expire.duration.minutes}")
	private long jwtTokenExpireDurationInMinutes;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(jwtSecretText.getBytes(StandardCharsets.UTF_8));
	}

	public String generateJwtToken(long userLoginId, String emailAddress, String userRole) {
		Instant now = Instant.now();
		Instant expiration = now.plus(jwtTokenExpireDurationInMinutes, ChronoUnit.MINUTES);

		String jwtToken = Jwts.builder()
				.claim("emailAddress", emailAddress)
				.claim("roles", List.of(userRole))
				.subject(String.valueOf(userLoginId))
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(secretKey)
				.compact();
		return jwtToken;
	}
}
