package com.dare.digitalbankingapi.integration;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountStatusRequest;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
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
import java.math.RoundingMode;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class AccountIntegrationTest {

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

	private void createAccount(Long userId, AccountRequest accountRequest) throws Exception {
		mockMvc.perform(post("/api/customer/accounts")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(accountRequest)))
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

	private String getAccountNumber(Long userId, Long accountId) throws Exception {
		String response = mockMvc.perform(get("/api/customer/accounts/" + accountId)
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return (JsonPath.read(response, "$.accountNumber"));
	}

	private String getAccountStatus(Long userId, Long accountId) throws Exception {
		String response = mockMvc.perform(get("/api/customer/accounts/" + accountId)
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return (JsonPath.read(response, "$.status"));
	}

	private double getAccountBalance(Long userId, Long accountId) throws Exception {
		String response = mockMvc.perform(get("/api/customer/accounts/" + accountId)
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return (JsonPath.read(response, "$.balance"));
	}

	private void depositToAccount(TransactionRequest transactionRequest) throws Exception {
		mockMvc.perform(post("/api/staff/accounts/deposits")
						.with(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transactionRequest)))
				.andExpect(status().isOk());
	}

	private void setAccountStatusToBlocked(Long accountId) throws Exception {

		mockMvc.perform(patch("/api/staff/accounts/"+ accountId + "/status")
				.with(adminAuth())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AccountStatusRequest(AccountStatus.BLOCKED))))
				.andExpect(status().isNoContent());
	}

	//Test

	@Test
	public void createAccount_whenRequestIsValid_returnsOK() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();

		createProfile(profileRequest, userId);
		AccountRequest accountRequest = new AccountRequest(Currency.EUR);

		mockMvc.perform(post("/api/customer/accounts")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(accountRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value(profileRequest.getName()))
				.andExpect(jsonPath("$.surname").value(profileRequest.getSurname()))
				.andExpect(jsonPath("$.accountNumber").exists())
				.andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
				.andExpect(jsonPath("$.currency").value(accountRequest.getCurrency().name()))
				.andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.name()));

	}

	@Test
	public void createAccount_whenNoUserProfileExists_returnsNotFound() throws Exception {

		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);

		mockMvc.perform(post("/api/customer/accounts")
						.with(customerAuth(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(accountRequest)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Could not find profile for user id " + userId));

	}

	@Test
	public void getAllAccountsByAuthenticatedUser_whenTwoAccountsExist_returnsListOfAccountResponses() throws Exception {

		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		AccountRequest firstAccount = new AccountRequest(Currency.EUR);
		AccountRequest secondAccount = new AccountRequest(Currency.USD);

		UserProfileRequest profileRequest = returnProfileRequest();

		createProfile(profileRequest, userId);

		createAccount(userId, firstAccount);
		createAccount(userId, secondAccount);

		mockMvc.perform(get("/api/customer/accounts/all")
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0,1].id").exists())
				.andExpect(jsonPath("$.[0].currency").value(firstAccount.getCurrency().name()))
				.andExpect(jsonPath("$.[1].currency").value(secondAccount.getCurrency().name()));


	}

	@Test
	public void getAllAccountsByAuthenticatedUser_whenNoAccountsExist_returnsEmptyListOfAccountResponses() throws Exception {

		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();

		createProfile(profileRequest, userId);

		mockMvc.perform(get("/api/customer/accounts/all")
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(0));

	}

	@Test
	public void getAccountById_whenAccountExists_returnsAccountResponse() throws Exception {

		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);

		UserProfileRequest profileRequest = returnProfileRequest();

		createProfile(profileRequest, userId);

		Long accountId = createAccountAndGetId(userId, accountRequest);

		mockMvc.perform(get("/api/customer/accounts/" + accountId)
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(accountId))
				.andExpect(jsonPath("$.accountNumber").exists())
				.andExpect(jsonPath("$.balance").value(BigDecimal.ZERO.doubleValue()))
				.andExpect(jsonPath("$.currency").value(accountRequest.getCurrency().name()))
				.andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.name()));
	}

	@Test
	public void getAccountById_whenAccountExistsButUserIsNotOwner_returnsNotFound() throws Exception {

		CustomerUserRequest loggedInUser = new CustomerUserRequest(EMAIL, PASSWORD);
		Long loggedInUserId = createUserAndGetId(loggedInUser);

		UserProfileRequest loggedInUserProfileRequest = returnProfileRequest();
		createProfile(loggedInUserProfileRequest, loggedInUserId);

		CustomerUserRequest owner = new CustomerUserRequest("secondUserEmail@mail.com", PASSWORD);
		Long ownerId = createUserAndGetId(owner);

		UserProfileRequest ownerProfileRequest = returnProfileRequest();
		createProfile(ownerProfileRequest, ownerId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(ownerId, accountRequest);

		mockMvc.perform(get("/api/customer/accounts/" + accountId)
						.with(customerAuth(loggedInUserId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Account with id " + accountId + " not found"));
	}

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
	public void closeAccount_whenRequestIsValid_returnsAccountClosureResponse() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);
		String accountNumber = getAccountNumber(userId, accountId);

		mockMvc.perform(post("/api/customer/accounts/" + accountId + "/close")
						.with(customerAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountId").value(accountId))
				.andExpect(jsonPath("$.accountNumber").value(accountNumber))
				.andExpect(jsonPath("$.message").value(
						"Account with id " + accountId
								+ " and account number " + accountNumber
								+ " has been closed"));
	}

	@Test
	public void closeAccount_whenAccountStatusIsInvalidAndBalanceIsNotZero_returnsBadRequest() throws Exception {
		CustomerUserRequest registerRequest = new CustomerUserRequest(EMAIL, PASSWORD);
		Long userId = createUserAndGetId(registerRequest);

		UserProfileRequest profileRequest = returnProfileRequest();
		createProfile(profileRequest, userId);

		AccountRequest accountRequest = new AccountRequest(Currency.EUR);
		Long accountId = createAccountAndGetId(userId, accountRequest);

		TransactionRequest deposit = new TransactionRequest(accountId, BigDecimal.valueOf(1000));
		depositToAccount(deposit);

		setAccountStatusToBlocked(accountId);
		BigDecimal balance = BigDecimal.valueOf(getAccountBalance(userId, accountId))
				.setScale(2, RoundingMode.HALF_UP);

		String accountStatus = getAccountStatus(userId, accountId);

		mockMvc.perform(post("/api/customer/accounts/" + accountId + "/close")
						.with(customerAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"Account with id " + accountId
								+ " cannot be closed. Current status: " + accountStatus
								+ ", current balance: " + balance
								+ ". Account must be ACTIVE and have zero balance."));
	}




}
