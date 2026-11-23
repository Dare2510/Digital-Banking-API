package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class TransactionIntegrationTest {

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
		accountRepository.deleteAll();
		userProfileRepository.deleteAll();
		userRepository.deleteAll();
	}

	//Tests

	//Customers

	@Test
	public void withdrawFromAccount_whenRequestIsValid_returnsTransactionResponse() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);

		TransactionRequest deposit = new TransactionRequest(accountId, BigDecimal.valueOf(1000));
		depositToAccount(deposit);

		TransactionRequest withdraw = new TransactionRequest(accountId, BigDecimal.valueOf(500));

		mockMvc.perform(post("/api/customer/accounts/withdrawals")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(withdraw)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromAccountId").value(accountId))
				.andExpect(jsonPath("$.balanceAfter").value(BigDecimal.valueOf(500).doubleValue()))
				.andExpect(jsonPath("$.amount").value(withdraw.getAmount().doubleValue()));

	}

	@Test
	public void withdrawFromAccount_whenAmountExceedsBalance_returnsBadRequest() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);

		TransactionRequest withdraw = new TransactionRequest(accountId, BigDecimal.valueOf(1000));

		mockMvc.perform(post("/api/customer/accounts/withdrawals")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(withdraw)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Account has insufficient balance for the requested withdrawal"));

	}

	@Test
	public void getAllTransactionsForUser_whenTransactionsAreFound_returnsListOfTransactionResponses() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);

		TransactionRequest deposit = new TransactionRequest(accountId, BigDecimal.valueOf(1000));
		TransactionRequest withdraw = new TransactionRequest(accountId, BigDecimal.valueOf(500));
		depositToAccount(deposit);
		withdrawFromAccount(userId,accountId,withdraw);

		mockMvc.perform(get("/api/customer/transactions")
				.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0].fromAccountId").value(accountId))
				.andExpect(jsonPath("$[1].fromAccountId").value(accountId))
				.andExpect(jsonPath("$[0].amount").value(deposit.getAmount().doubleValue()))
				.andExpect(jsonPath("$[1].amount").value(withdraw.getAmount().doubleValue()))
				.andExpect(jsonPath("$[0].transactionType").value(TransactionType.DEPOSIT.name()))
				.andExpect(jsonPath("$[1].transactionType").value(TransactionType.WITHDRAW.name()));
	}

	@Test
	public void getAllTransactionsForUser_whenNoTransactionsAreFound_returnsEmptyList() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		mockMvc.perform(get("/api/customer/transactions")
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(0))
				.andExpect(jsonPath("$[0]").doesNotExist());

	}

	@Test
	public void getTransactionsById_whenTransactionIsFound_returnsIsOK() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);

		TransactionRequest deposit = new TransactionRequest(accountId, BigDecimal.valueOf(1000));
		Long transactionId = depositToAccountAndGetId(deposit);

		mockMvc.perform(get("/api/customer/transactions/" + transactionId)
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromAccountId").value(accountId))
				.andExpect(jsonPath("$.amount").value(deposit.getAmount().doubleValue()))
				.andExpect(jsonPath("$.balanceAfter").value(BigDecimal.valueOf(1000).doubleValue()));
	}

	@Test
	public void getTransactionsById_whenUserIsNotOwnerOfTransaction_returnsIsNotFound() throws Exception {
		CustomerUserRequest loggedInUser = new CustomerUserRequest(EMAIL, PASSWORD);
		Long loggedInUserId = createUserAndGetId(loggedInUser);

		CustomerUserRequest owner = new CustomerUserRequest("different@mail.com", PASSWORD);
		Long ownerId = createUserAndGetId(owner);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, ownerId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(ownerId, accountRequest);

		TransactionRequest deposit = new TransactionRequest(accountId, BigDecimal.valueOf(1000));
		Long transactionId = depositToAccountAndGetId(deposit);

		mockMvc.perform(get("/api/customer/transactions/" + transactionId)
						.with(customerAuth(loggedInUserId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Transaction for transfer Id " + transactionId + " not found"));
	}

	//Staff

	@Test
	public void depositToAccount_whenRequestIsValid_returnsIsOK() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);

		TransactionRequest deposit = new TransactionRequest(accountId, BigDecimal.valueOf(1000));

		//first deposit to check correct balance calculation
		depositToAccount(deposit);

		mockMvc.perform(post("/api/staff/accounts/deposits")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deposit)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromAccountId").value(accountId))
				.andExpect(jsonPath("$.amount").value(deposit.getAmount().doubleValue()))
				.andExpect(jsonPath("$.balanceAfter").value(BigDecimal.valueOf(2000).doubleValue()));

	}

	@Test
	public void getPageOfTransactions_withPageableDefault_returnsIsOK() throws Exception {
		mockMvc.perform(get("/api/staff/transactions")
				.with(adminAuth())
						.param("page", "0")
						.param("size", "20")
						.param("sort", "createdAt")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(20));
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
