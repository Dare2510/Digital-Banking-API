package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

	private TransferService transferService;

	@Mock
	private TransferRepository transferRepository;

	@Mock
	private AccountService accountService;

	@Mock
	private TransactionService transactionService;

	@Spy
	private SortValidator sortValidator;

	private final Long TRANSFER_ID = 1L;

	@BeforeEach
	void setUp() {
		transferService = new TransferService(transferRepository, accountService, transactionService, sortValidator);

	}

	//Helper
	private UserProfileEntity returnUserProfileEntity() {
		return new UserProfileEntity(
				returnUserEntity(),
				"testName", "testSurName", "testStreet",
				"99", "testCity", "testZipCode", "testCountry"
		);
	}

	private UserEntity returnUserEntity() {
		UserEntity customer = new UserEntity(
				"testEmail@mail.com",
				"passwordHash",
				UserRole.CUSTOMER
		);

		customer.setId(1L);
		return customer;

	}

	private AuthenticatedUser returnAuthenticatedUser(UserEntity user) {
		return new AuthenticatedUser(user.getId(), user.getEmail(), user.getUserRole());
	}

	private void setAccountValues(AccountEntity accountEntity, Long accountId, String accountNumber,
								  UserEntity userEntity, AccountRequest accountRequest) {
		accountEntity.setId(accountId);
		accountEntity.setOwner(userEntity);
		accountEntity.setAccountNumber(accountNumber);
		accountEntity.setCurrency(accountRequest.getCurrency());
		accountEntity.setStatus(AccountStatus.ACTIVE);
		accountEntity.setCreatedAt(LocalDateTime.now());


	}

	@Test
	public void createTransferBetweenAccounts_whenRequestIsValid_returnsTransferResponse() {
		AccountEntity fromAccount = new AccountEntity();
		Long fromAccountId = 1L;
		String fromAccountNumber = "fromAccountNumber";
		UserEntity fromUser = returnUserEntity();
		fromAccount.setId(1L);

		setAccountValues(fromAccount, fromAccountId, fromAccountNumber, fromUser, new AccountRequest(Currency.EUR));
		fromAccount.setOwner(fromUser);
		fromAccount.setBalance(new BigDecimal("1000"));

		AccountEntity toAccount = new AccountEntity();
		Long toAccountId = 2L;
		String toAccountNumber = "toAccountNumber";
		UserEntity toUser = returnUserEntity();
		toUser.setId(2L);
		toAccount.setOwner(toUser);

		setAccountValues(toAccount, toAccountId, toAccountNumber, toUser, new AccountRequest(Currency.EUR));
		toAccount.setBalance(new BigDecimal("100"));

		AuthenticatedUser loggedInUser = returnAuthenticatedUser(fromUser);

		BigDecimal amount = new BigDecimal("100");
		String referenceNumber = "referenceNumber";

		TransferRequest transferRequest = new TransferRequest(fromAccountId, toAccountId, amount, referenceNumber);


		TransferResponse response = transferService.createTransferBetweenAccounts(loggedInUser, transferRequest);

		assertEquals(fromAccount.getBalance(), BigDecimal.valueOf(900));
		assertEquals(toAccount.getBalance(), BigDecimal.valueOf(200));
		assertEquals(fromAccountId, response.getFromAccountId());
		assertEquals(toAccountId, response.getToAccountId());
		assertEquals(TRANSFER_ID, response.getTransferId());
		assertEquals(amount, response.getAmount());
		assertEquals(referenceNumber, response.getReference());

	}

}
