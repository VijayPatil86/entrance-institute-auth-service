create table USER_LOGIN (
	USER_LOGIN_ID bigserial primary key,
	EMAIL_ADDRESS varchar(254) not null unique,
	HASHED_PASSWORD varchar(200) not null,
	STATUS varchar(20) not null default 'PENDING_VERIFICATION' check(STATUS in ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED')),
	VERIFICATION_TOKEN varchar(100),
	VERIFICATION_TOKEN_EXPIRES_AT timestamp with time zone,
	CREATED_AT timestamp with time zone default current_timestamp not null,
	UPDATED_AT timestamp with time zone default current_timestamp not null
);