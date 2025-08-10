package com.neec.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neec.config.SecurityConfig;
import com.neec.dto.LoginRequestDTO;
import com.neec.dto.RegistrationRequestDTO;
import com.neec.exception.UserAccountSuspendedException;
import com.neec.exception.UserAlreadyExistsException;
import com.neec.exception.UserNotFoundException;
import com.neec.exception.UserNotVerifiedException;
import com.neec.service.AuthenticationService;

@WebMvcTest(controllers = AuthenticationController.class)
@Import(SecurityConfig.class)
public class AuthenticationControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private AuthenticationService mockAuthenticationService;
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	void test_registerUser_NullBody_Returns_400_BAD_REQUEST() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
	}
	
	@Test
	void test_registerUser_EmptyJSON_Returns_400_BAD_REQUEST() throws Exception {
		String emptyJSON = "{}";
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(emptyJSON);
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertTrue(jsonNode.get("emailAddress").asText().equals("Email address cannot be blank."));
		assertTrue(jsonNode.get("password").asText().equals("Password cannot be blank."));
	}
	
	@Test
	void test_registerUser_FieldsWithNullValues_Returns_400_BAD_REQUEST() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress(null)
				.password(null)
				.build();
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertTrue(jsonNode.get("emailAddress").asText().equals("Email address cannot be blank."));
		assertTrue(jsonNode.get("password").asText().equals("Password cannot be blank."));
	}
	
	@Test
	void test_registerUser_FieldsWithEmptyStrings_Returns_400_BAD_REQUEST() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("")
				.password("")
				.build();
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertNotNull(jsonNode.get("emailAddress"));
		assertNotNull(jsonNode.get("password"));
	}
	
	@Test
	void test_registerUser_FieldsWithInvalidValues_Returns_400_BAD_REQUEST() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("abc$gamil#com")
				.password("password")
				.build();
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertNotNull(jsonNode.get("emailAddress"));
		assertNotNull(jsonNode.get("password"));
	}
	
	@Test
	void test_registerUser_FieldsWithValidValues_Returns_201_CREATED() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("abc@gmail.com")
				.password("P@$$w0rd")
				.build();
		doNothing().when(mockAuthenticationService).registerUser(any(RegistrationRequestDTO.class));
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		verify(mockAuthenticationService, times(1)).registerUser(any(RegistrationRequestDTO.class));
		assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
	}
	
	@Test
	void test_registerUser_DuplicateEmailAddress_Returns_409_CONFLICT() throws Exception {
		RegistrationRequestDTO dto = RegistrationRequestDTO.builder()
				.emailAddress("duplicate.email@gmail.com")
				.password("P@$$w0rd")
				.build();
		doThrow(new UserAlreadyExistsException("Email is already used."))
			.when(mockAuthenticationService).registerUser(any(RegistrationRequestDTO.class));
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		verify(mockAuthenticationService, times(1)).registerUser(any(RegistrationRequestDTO.class));
		assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
	}
	
	@Test
	void testLogin_EmailAddressNotFound_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("email.address.not.found@gmail.com")
				.password("P@$$w0rd")
				.build();
		doThrow(new UserNotFoundException("invalid email or password."))
			.when(mockAuthenticationService).login(any(LoginRequestDTO.class));
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		verify(mockAuthenticationService, times(1)).login(any(LoginRequestDTO.class));
		assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertEquals("invalid email or password.", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_EmailAddressExists_InvalidPassword_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("email.address.found@gmail.com")
				.password("invalid-password")
				.build();
		doThrow(new UserNotFoundException("invalid email or password."))
			.when(mockAuthenticationService).login(any(LoginRequestDTO.class));
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		verify(mockAuthenticationService, times(1)).login(any(LoginRequestDTO.class));
		assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertEquals("invalid email or password.", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountSuspended_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("email.address.found@gmail.com")
				.password("valid-password")
				.build();
		doThrow(new UserAccountSuspendedException("Your account is suspended. Please contact Administrator"))
			.when(mockAuthenticationService).login(any(LoginRequestDTO.class));
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		verify(mockAuthenticationService, times(1)).login(any(LoginRequestDTO.class));
		assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertEquals("Your account is suspended. Please contact Administrator", jsonNode.get("error").asText());
	}

	@Test
	void testLogin_ValidLoginCredentials_AccountNotVerified_Return_401_UNAUTHORIZED() throws Exception {
		LoginRequestDTO dto = LoginRequestDTO.builder()
				.emailAddress("email.address.found@gmail.com")
				.password("valid-password")
				.build();
		doThrow(new UserNotVerifiedException("Your account is not verified. Please check your email."))
			.when(mockAuthenticationService).login(any(LoginRequestDTO.class));
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJsonString(dto));
		MvcResult result = mockMvc.perform(request)
				.andDo(print())
				.andReturn();
		verify(mockAuthenticationService, times(1)).login(any(LoginRequestDTO.class));
		assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
		JsonNode jsonNode = toJsonNode(result.getResponse().getContentAsString());
		assertEquals("Your account is not verified. Please check your email.", jsonNode.get("error").asText());
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
