package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.dto.EmployeeUserRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	//Fields

	private final String EMAIL = "testusermail@mail.com";
	private final String PASSWORD = "password";
	private final UserRole CUSTOMER_ROLE = UserRole.CUSTOMER;

	@AfterEach
	public void tearDown() {
		userProfileRepository.deleteAll();
		userRepository.deleteAll();

	}

	//Tests

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

	@Test
	public void updateProfile_withValidRequest_returnsUserProfileResponse() throws Exception {
		CustomerUserRequest validRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(validRequest);

		UserProfileRequest profileRequest = returnRequest();
		createProfile(profileRequest, userId);

		UserProfileRequest updatedProfileRequest = returnUpdateRequest();

		mockMvc.perform(patch("/api/auth/register/profile/update")
						.with(auth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedProfileRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(updatedProfileRequest.getName()))
				.andExpect(jsonPath("$.surname").value(updatedProfileRequest.getSurname()))
				.andExpect(jsonPath("$.street").value(updatedProfileRequest.getStreet()))
				.andExpect(jsonPath("$.houseNumber").value(updatedProfileRequest.getHouseNumber()))
				.andExpect(jsonPath("$.city").value(updatedProfileRequest.getCity()))
				.andExpect(jsonPath("$.zipCode").value(updatedProfileRequest.getZipCode()))
				.andExpect(jsonPath("$.country").value(updatedProfileRequest.getCountry()));
	}

	@Test
	public void updateProfile_whenNoProfileExists_returnsConflict() throws Exception {
		CustomerUserRequest validRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(validRequest);

		UserProfileRequest updatedProfileRequest = returnUpdateRequest();

		mockMvc.perform(patch("/api/auth/register/profile/update")
						.with(auth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedProfileRequest)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message")
						.value("Profile update failed for user id: " + userId + "no profile exists"));
	}

	//Staff

	@Test
	public void getPageOfUsersWithDetails_withPageableDefaults_returnsIsOK() throws Exception {
		mockMvc.perform(get("/api/staff/users")
						.with(adminAuth())
						.param("page", "0")
						.param("size", "20")
						.param("sort", "id")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(20));
	}

	//Admin
	@Test
	public void createEmployeeUser_withValidRequest_returnsIsOK() throws Exception {
		EmployeeUserRequest validRequest = new EmployeeUserRequest(EMAIL, PASSWORD, UserRole.STAFF);

		mockMvc.perform(post("/api/admin/employee")
				.with(adminAuth())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.email").value(validRequest.getEmail()))
				.andExpect(jsonPath("$.userRole").value(validRequest.getUserRole().name()));
	}

	@Test
	public void createEmployeeProfile_withValidRequest_returnsIsOK() throws Exception {
		EmployeeUserRequest validRequest = new EmployeeUserRequest(EMAIL, PASSWORD, UserRole.STAFF);
		Long employeeUserId = createEmployeeUserAndGetId(validRequest);

		UserProfileRequest profileRequest = returnRequest();

		mockMvc.perform(post("/api/admin/employee/" + employeeUserId + "/profile")
						.with(adminAuth())
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
	public void updateEmployeeProfile_withValidRequest_returnsIsOK() throws Exception {
		EmployeeUserRequest validRequest = new EmployeeUserRequest(EMAIL, PASSWORD, UserRole.STAFF);
		Long employeeUserId = createEmployeeUserAndGetId(validRequest);

		UserProfileRequest profileRequest = returnRequest();
		createEmployeeProfile(profileRequest, employeeUserId);

		UserProfileRequest updatedProfileRequest = returnUpdateRequest();

		mockMvc.perform(patch("/api/admin/employee/" + employeeUserId + "/profile/update")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedProfileRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(updatedProfileRequest.getName()))
				.andExpect(jsonPath("$.surname").value(updatedProfileRequest.getSurname()))
				.andExpect(jsonPath("$.street").value(updatedProfileRequest.getStreet()))
				.andExpect(jsonPath("$.houseNumber").value(updatedProfileRequest.getHouseNumber()))
				.andExpect(jsonPath("$.city").value(updatedProfileRequest.getCity()))
				.andExpect(jsonPath("$.zipCode").value(updatedProfileRequest.getZipCode()))
				.andExpect(jsonPath("$.country").value(updatedProfileRequest.getCountry()));
	}

	@Test
	public void updateUserRole_withValidRequest_returnsIsOK() throws Exception {
		EmployeeUserRequest validRequest = new EmployeeUserRequest(EMAIL, PASSWORD, UserRole.STAFF);
		Long employeeUserId = createEmployeeUserAndGetId(validRequest);

		mockMvc.perform(patch("/api/admin/users/" + employeeUserId + "/userRole")
				.with(adminAuth())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(UserRole.ADMIN.name())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.userRole").value(UserRole.ADMIN.name()));

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

	private UserProfileRequest returnUpdateRequest() {
		return new UserProfileRequest(
				"updatedName",
				"updatedSurname",
				"updatedStreet",
				"10",
				"updatedCity",
				"654321",
				"updatedCountry"
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

	private RequestPostProcessor adminAuth() {
		AuthenticatedUser customer = new AuthenticatedUser(
				99L, EMAIL, UserRole.ADMIN
		);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				customer,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name())));

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

	private void createProfile(UserProfileRequest profileRequest, Long userId) throws Exception {
		mockMvc.perform(post("/api/auth/register/profile")
						.with(auth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(profileRequest)))
				.andExpect(status().isOk());
	}

	private Long createEmployeeUserAndGetId(EmployeeUserRequest employeeUserRequest) throws Exception {
		String response = mockMvc.perform(post("/api/admin/employee")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(employeeUserRequest)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private void createEmployeeProfile(UserProfileRequest profileRequest, Long employeeUserId) throws Exception {
		mockMvc.perform(post("/api/admin/employee/" + employeeUserId + "/profile")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(profileRequest)))
				.andExpect(status().isOk());
	}


}
