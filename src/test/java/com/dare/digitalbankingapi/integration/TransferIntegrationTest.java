package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class TransferIntegrationTest {

	@Container
	@ServiceConnection
	public static PostgreSQLContainer postgreSQLContainer =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private TransferRepository transferRepository;

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
		transactionRepository.deleteAll();
		transferRepository.deleteAll();
		accountRepository.deleteAll();
		userProfileRepository.deleteAll();
		userRepository.deleteAll();
	}

	//Tests

	@Test
	public void createTransferBetweenAccounts_whenRequestIsValid_returnsIsOK() throws Exception {
		//From User
		CustomerUserRequest loggedInUser = new CustomerUserRequest(EMAIL, PASSWORD);
		Long loggedInUserId = createUserAndGetId(loggedInUser);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, loggedInUserId);

		//To User
		CustomerUserRequest toUser = new CustomerUserRequest("toUserMail@mail.com", PASSWORD);
		Long toUserId = createUserAndGetId(toUser);

		createProfile(profileRequest, toUserId);
		//create Accounts

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long fromAccountId = createAccountAndGetId(loggedInUserId, accountRequest);
		Long toAccountId = createAccountAndGetId(toUserId, accountRequest);

		depositToAccount(new TransactionRequest(fromAccountId, BigDecimal.valueOf(1000)));

		//Transfer
		TransferRequest transferRequest =
				new TransferRequest(fromAccountId, toAccountId, BigDecimal.valueOf(500),"reference");

		mockMvc.perform(post("/api/customer/transfers")
				.with(customerAuth(loggedInUserId))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transferRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromAccountId").value(fromAccountId))
				.andExpect(jsonPath("$.toAccountId").value(toAccountId))
				.andExpect(jsonPath("$.transferId").exists())
				.andExpect(jsonPath("$.amount").value(500))
				.andExpect(jsonPath("$.reference").value("reference"));

	}

	//Helper

	private UserProfileRequest returnProfileRequest() {
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

	private RequestPostProcessor customerAuth(Long userId) {
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
		//99 -> random admin id
		AuthenticatedUser admin = new AuthenticatedUser(
				99L, "adminMail@mail.com", UserRole.ADMIN
		);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				admin,
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
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(profileRequest)))
				.andExpect(status().isOk());
	}

	private Long createAccountAndGetId(Long userId, AccountRequest accountRequest) throws Exception {
		String response = mockMvc.perform(post("/api/customer/accounts")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(accountRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private void depositToAccount(TransactionRequest transactionRequest) throws Exception {
		mockMvc.perform(post("/api/staff/accounts/deposits")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transactionRequest)))
				.andExpect(status().isOk());
	}

	private Long depositToAccountAndGetId(TransactionRequest transactionRequest) throws Exception {
		String response = mockMvc.perform(post("/api/staff/accounts/deposits")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transactionRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(response, "$.id")).longValue();

	}

	private void withdrawFromAccount(Long userId, Long accountId,TransactionRequest transactionRequest) throws Exception {
		mockMvc.perform(post("/api/customer/accounts/withdrawals")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transactionRequest)))
				.andExpect(status().isOk());
	}
}
