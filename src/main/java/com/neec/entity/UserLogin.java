package com.neec.entity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.neec.enums.EnumRole;
import com.neec.enums.EnumUserAccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder @AllArgsConstructor @NoArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE) @Getter @Setter
@Entity
@Table(name="USER_LOGIN")
public class UserLogin {
	@Column(name="USER_LOGIN_ID")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long userLoginId;
	
	@Column(name="EMAIL_ADDRESS", nullable = false, unique = true, insertable = true, updatable = false)
	private String emailAddress;
	
	@Column(name = "HASHED_PASSWORD", insertable = true, updatable = true, nullable = false, unique = false)
	private String hashedPassword;
	
	@Column(name="STATUS", insertable = true, updatable = true, nullable = false, unique = false)
	@Enumerated(EnumType.STRING)
	private EnumUserAccountStatus accountStatus;
	
	@Column(name="VERIFICATION_TOKEN", insertable = true, updatable = true, nullable = true, unique = true)
	private String verificationToken;
	
	@Column(name="VERIFICATION_TOKEN_EXPIRES_AT", insertable = true, updatable = true, nullable = true, unique = false)
	private OffsetDateTime verificationTokenExpiresAt;
	
	@Column(name="CREATED_AT", insertable = true, updatable = false, nullable = false, unique = false)
	private OffsetDateTime createdAt;
	
	@Column(name="UPDATED_AT", insertable = true, updatable = true, nullable = false, unique = false)
	private OffsetDateTime updatedAt;

	@Column(name = "ROLE", insertable = true, updatable = true, nullable = false, unique = false)
	@Enumerated(EnumType.STRING)
	private EnumRole role;

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
	
	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
	}
}
