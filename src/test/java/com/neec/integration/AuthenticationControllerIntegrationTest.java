package com.neec.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neec.dto.LoginRequestDTO;
import com.neec.dto.RegistrationRequestDTO;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class AuthenticationControllerIntegrationTest {
	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void test_registerUser_NullBody_Returns_400_BAD_REQUEST() throws URISyntaxException {
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(null);
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void test_registerUser_EmptyJSON_Returns_400_BAD_REQUEST() throws Exception {
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body("{}");
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertTrue(jsonNode.get("emailAddress").asText().equals("Email address cannot be blank."));
		assertTrue(jsonNode.get("password").asText().equals("Password cannot be blank."));
	}

	@Test
	void test_registerUser_FieldsWithNullValues_Returns_400_BAD_REQUEST() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress(null)
				.password(null)
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertTrue(jsonNode.get("emailAddress").asText().equals("Email address cannot be blank."));
		assertTrue(jsonNode.get("password").asText().equals("Password cannot be blank."));
	}

	@Test
	void test_registerUser_FieldsWithEmptyStrings_Returns_400_BAD_REQUEST() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("")
				.password("")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertNotNull(jsonNode.get("emailAddress"));
		assertNotNull(jsonNode.get("password"));
	}

	@Test
	void test_registerUser_FieldsWithInvalidValues_Returns_400_BAD_REQUEST() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("abc$gamil#com")
				.password("password")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertNotNull(jsonNode.get("emailAddress"));
		assertNotNull(jsonNode.get("password"));
	}

	@Test
	void test_registerUser_FieldsWithValidValues_Returns_201_CREATED() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("abc_01@gmail.com")
				.password("P@$$w0rd")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	void test_registerUser_DuplicateEmailAddress_Returns_409_CONFLICT() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("abc_01@gmail.com")
				.password("P@$$w0rd")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/register"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	}

	@Test
	void testLogin_EmailAddressNotFound_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("email.address.not.found@gmail.com")
				.password("P@$$w0rd")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/login"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("invalid email or password.", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_EmailAddressExists_InvalidPassword_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("abc_01@gmail.com")
				.password("invalid-password")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/login"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("invalid email or password.", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountNotVerified_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("abc_01@gmail.com")
				.password("P@$$w0rd")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/login"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("Your account is not verified. Please check your email.", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountSuspended_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("donald@gmail.com")
				.password("P@$$w0rd")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/login"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("Your account is suspended. Please contact Administrator", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountVerified_Return_200_OK() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("def@gmail.com")
				.password("P@$$w0rd")
				.build();
		RequestEntity<Object> request = RequestEntity.post(URI.create("/api/v1/auth/login"))
				.contentType(MediaType.APPLICATION_JSON)
				.body(toJsonString(dto));
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertNotNull(jsonNode.get("jwtToken").asText());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/api/v1/auth/verify", "/api/v1/auth/verify?", "/api/v1/auth/verify?t",
			"/api/v1/auth/verify?tokn"})
	void testVerifyEmailAddress_URL_Without_Token_Word(String url) throws Exception {
		RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("Request parameter 'token' is required.", jsonNode.get("error").asText());
	}

	@Test
	void testVerifyEmailAddress_URL_With_EmptyToken() throws Exception {
		RequestEntity<Void> request = RequestEntity.get(URI.create("/api/v1/auth/verify?token=")).build();
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("Verification token can not be empty.", jsonNode.get("error").asText());
	}

	@Test
	void testVerifyEmailAddress_URL_With_InvalidToken() throws Exception {
		RequestEntity<Void> request = RequestEntity.get(URI.create("/api/v1/auth/verify?token=invalid-token")).build();
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("invalid or expired verification token.", jsonNode.get("error").asText());
	}

	@Test
	void testVerifyEmailAddress_URL_With_ExpiredToken() throws Exception {
		String url = "/api/v1/auth/verify?token=797f5c56-7b5b-40bc-aafe-1c91e048434d";
		RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("Verification token has expired.", jsonNode.get("error").asText());
	}

	@Test
	void testVerifyEmailAddress_URL_With_ActiveToken() throws Exception {
		String url = "/api/v1/auth/verify?token=c70ab85f-a014-42d7-92c0-f6d8246d43e8";
		RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
		ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JsonNode jsonNode = toJsonNode(response.getBody());
		assertEquals("Your email address has been successfully verified. You can now log in.", jsonNode.get("status").asText());
	}

	private String toJsonString(RegistrationRequestDTO dto) throws JsonProcessingException {
		return objectMapper.writeValueAsString(dto);
	}

	private String toJsonString(LoginRequestDTO dto) throws JsonProcessingException {
		return objectMapper.writeValueAsString(dto);
	}

	private JsonNode toJsonNode(String jsonString) throws JsonMappingException, JsonProcessingException {
		return objectMapper.readTree(jsonString);
	}
}
