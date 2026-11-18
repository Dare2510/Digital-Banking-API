package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.security.dto.LoginRequest;
import com.dare.digitalbankingapi.security.jwt.JwtUtil;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class AuthIntegrationTest {

	@Container
	@ServiceConnection
	public static PostgreSQLContainer postgreSQLContainer =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtUtil jwtUtil;

	private final String EMAIL = "testUserMail@mail.com";
	private final String PASSWORD = "password";
	private final UserRole CUSTOMER_ROLE = UserRole.CUSTOMER;

	@AfterEach
	public void cleanUp() {userRepository
			.findByEmail(EMAIL)
			.ifPresent(user -> userRepository.delete(user));}

	@Test
	public void loginTest_requestIsValid_returnJWT() throws Exception {
		CustomerUserRequest request = newCustomerUserRequest();
		Long userId = createUserAndGetId(request);

		LoginRequest validLoginRequest = validRequest();
		String jwt = generateToken(userId);

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validLoginRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		assertEquals(jwt.length(), response.length());
		assertEquals(EMAIL.toLowerCase(Locale.ROOT), jwtUtil.extractEmail(response));
		assertEquals(userId,jwtUtil.extractUserId(response));
		assertEquals(CUSTOMER_ROLE.name(),jwtUtil.extractUserRole(response));

	}

	@Test
	public void loginTest_requestWithInvalidPassword_returnsUnauthorized() throws Exception {
		CustomerUserRequest request = newCustomerUserRequest();
		createCustomer(request);

		LoginRequest invalidLoginRequest = invalidPassword();

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidLoginRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message")
						.value("Password and/or Email input is incorrect"));
	}




	//Helper
	private CustomerUserRequest newCustomerUserRequest() {
		return new CustomerUserRequest(
				EMAIL,
				PASSWORD
		);
	}

	private LoginRequest validRequest() {
		return new LoginRequest(
				EMAIL,
				PASSWORD
		);
	}

	private LoginRequest invalidPassword() {
		return new LoginRequest(
				EMAIL,
				"incorrectPassword"
		);
	}

	private String generateToken(Long userId) {
		return jwtUtil.generateToken(EMAIL,CUSTOMER_ROLE,userId);
	}

	private Long createUserAndGetId(CustomerUserRequest customerUserRequest) throws Exception {
		String response = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerUserRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(response, "$.id")).longValue();

	}

	private void createCustomer(CustomerUserRequest customerUserRequest) throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerUserRequest)))
				.andExpect(status().isOk());
	}

}
