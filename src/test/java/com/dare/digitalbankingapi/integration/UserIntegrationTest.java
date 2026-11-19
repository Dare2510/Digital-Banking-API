package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

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

	private final String EMAIL = "testusermail@mail.com";
	private final String PASSWORD = "password";
	private final UserRole CUSTOMER_ROLE = UserRole.CUSTOMER;

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
		String invalidMail = "invalidMail";

		CustomerUserRequest validRequest = new CustomerUserRequest(invalidMail, PASSWORD);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Invalid email address"));
	}

	@Test
	public void registerCustomerUser_withInvalidPasswordValue_returnsBadRequest() throws Exception {
		String invalidPassword = "inv";

		CustomerUserRequest validRequest = new CustomerUserRequest(EMAIL, invalidPassword);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Password must be between 6 and 72 characters"));
	}

	@Test
	public void createProfile_withValidRequest_returnsUserProfileResponse() throws Exception {
		CustomerUserRequest validRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(validRequest);

		UserProfileRequest profileRequest = returnRequest();

		mockMvc.perform(post("/api/auth/register/profile")
						.with(auth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(profileRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(profileRequest.getName()))
				.andExpect(jsonPath("$.surname").value(profileRequest.getSurname()))
				.andExpect(jsonPath("$.street").value(profileRequest.getStreet()))
				.andExpect(jsonPath("$.houseNumber").value(profileRequest.getHouseNumber()))
				.andExpect(jsonPath("$.city").value(profileRequest.getCity()))
				.andExpect(jsonPath("$.zipCode").value(profileRequest.getZipCode()))
				.andExpect(jsonPath("$.country").value(profileRequest.getCountry()));
	}

	@Test
	public void createProfile_whenNoUserExists_returns401() throws Exception {

		UserProfileRequest profileRequest = returnRequest();

		mockMvc.perform(post("/api/auth/register/profile")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(profileRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message")
						.value("You must be authenticated to access this resource"));
	}


	//Helper

	private UserProfileRequest returnRequest() {
		return new UserProfileRequest(
				"Name",
				"Surname",
				"Street",
				"5",
				"City",
				"123456",
				"Country"
		);
	}

	private RequestPostProcessor auth(Long userId) {
		AuthenticatedUser customer = new AuthenticatedUser(
				userId, EMAIL, CUSTOMER_ROLE
		);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				customer,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_" + CUSTOMER_ROLE.name())));

		return authentication(auth);
	}

	private Long createUserAndGetId(CustomerUserRequest customerUserRequest) throws Exception {
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerUserRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(response, "$.id")).longValue();

	}


}
