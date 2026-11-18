package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.security.dto.LoginRequest;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class UserIntegrationTest {

	@Container
	@ServiceConnection
	public static PostgreSQLContainer postgreSQLContainer =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserProfileRepository userProfileRepository;

	@Autowired
	private ObjectMapper objectMapper;

	//valid field values
	private final String EMAIL = "testusermail@mail.com";
	private final String PASSWORD = "password";
	private final UserRole CUSTOMER_ROLE = UserRole.CUSTOMER;

	//invalid field values
	private final String INVALID_EMAIL = "invalidMail";
	private final String INVALID_PASSWORD = "inv";

	@AfterEach
	public void tearDown() {
		userRepository.deleteAll();
		userProfileRepository.deleteAll();
	}

	@Test
	public void registerCustomerUser_withValidRequest_returnsUserResponse() throws Exception {
		CustomerUserRequest validRequest = new CustomerUserRequest(EMAIL, PASSWORD);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isOk());
	}

	@Test
	public void registerCustomerUser_withInvalidEmailValue_returnsBadRequest() throws Exception {
		CustomerUserRequest validRequest = new CustomerUserRequest(INVALID_EMAIL, PASSWORD);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Invalid email address"));
	}

	@Test
	public void registerCustomerUser_withInvalidPasswordValue_returnsBadRequest() throws Exception {
		CustomerUserRequest validRequest = new CustomerUserRequest(EMAIL, INVALID_PASSWORD);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Password must be between 6 and 72 characters"));
	}


	//Helper

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

	private void loginCustomer(LoginRequest loginRequest) throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk());
	}


}
