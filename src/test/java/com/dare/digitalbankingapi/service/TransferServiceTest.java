package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.entity.TransferStatus;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

		Long fromAccountId = 1L;
		Long toAccountId = 2L;

		BigDecimal amount = new BigDecimal("100");
		String referenceNumber = "referenceNumber";

		UserEntity fromUser = returnUserEntity();
		UserEntity toUser = returnUserEntity();
		toUser.setId(2L);

		AccountEntity fromAccount = new AccountEntity();
		setAccountValues(
				fromAccount,
				fromAccountId,
				"fromAccountNumber",
				fromUser,
				new AccountRequest(Currency.EUR)
		);
		fromAccount.setId(fromAccountId);
		fromAccount.setOwner(fromUser);
		fromAccount.setBalance(new BigDecimal("1000"));

		AccountEntity toAccount = new AccountEntity();
		setAccountValues(
				toAccount,
				toAccountId,
				"toAccountNumber",
				toUser,
				new AccountRequest(Currency.EUR)
		);
		toAccount.setId(toAccountId);
		toAccount.setOwner(toUser);
		toAccount.setBalance(new BigDecimal("100"));

		AuthenticatedUser loggedInUser = returnAuthenticatedUser(fromUser);

		TransferRequest transferRequest = new TransferRequest(
				fromAccountId,
				toAccountId,
				amount,
				referenceNumber
		);


		when(accountService.getAccount(fromAccountId)).thenReturn(fromAccount);
		when(accountService.getAccount(toAccountId)).thenReturn(toAccount);
		when(accountService.userIsOwner(fromAccount, loggedInUser)).thenReturn(true);

		when(transferRepository.saveAndFlush(any(TransferEntity.class)))
				.thenAnswer(invocation -> {
					TransferEntity savedTransfer = invocation.getArgument(0);
					savedTransfer.setId(TRANSFER_ID);
					return savedTransfer;
				});

		doAnswer(invocation -> {
			AccountEntity account = invocation.getArgument(0);
			BigDecimal transferAmount = invocation.getArgument(1);
			TransactionType type = invocation.getArgument(2);

			if (type == TransactionType.TRANSFER_OUT) {
				account.setBalance(
						account.getBalance().subtract(transferAmount)
				);
			} else if (type == TransactionType.TRANSFER_IN) {
				account.setBalance(
						account.getBalance().add(transferAmount)
				);
			}

			return null;
		}).when(accountService).updateBalance(
				any(AccountEntity.class),
				any(BigDecimal.class),
				any(TransactionType.class));

		when(transactionService.createTransferTransaction(
				eq(fromAccount),
				eq(referenceNumber),
				any(TransferEntity.class),
				eq(TransactionType.TRANSFER_OUT)
		)).thenAnswer(invocation -> {
			TransferEntity savedTransfer = invocation.getArgument(2);

			return new TransactionEntity(
					fromAccount,
					amount,
					referenceNumber,
					TransactionType.TRANSFER_OUT,
					fromAccount.getBalance(),
					savedTransfer
			);
		});

		TransferResponse response = transferService.createTransferBetweenAccounts(loggedInUser, transferRequest);

		assertNotNull(response);

		assertEquals(new BigDecimal("900"), fromAccount.getBalance());
		assertEquals(new BigDecimal("200"), toAccount.getBalance());

		assertEquals(fromAccountId, response.getFromAccountId());
		assertEquals(toAccountId, response.getToAccountId());
		assertEquals(TRANSFER_ID, response.getTransferId());
		assertEquals(amount, response.getAmount());
		assertEquals(referenceNumber, response.getReference());

		verify(accountService).userIsOwner(fromAccount, loggedInUser);
		verify(accountService).checkAccountStatus(fromAccount);
		verify(accountService).checkAccountStatus(toAccount);

		verify(transferRepository).saveAndFlush(
				argThat(transfer ->
						transfer.getStatus() == TransferStatus.COMPLETED
								&& transfer.getFromAccount() == fromAccount
								&& transfer.getToAccount() == toAccount
				)
		);

		verify(accountService).updateBalance(fromAccount, amount, TransactionType.TRANSFER_OUT);
		verify(accountService).updateBalance(toAccount, amount, TransactionType.TRANSFER_IN);

		verify(transactionService).createTransferTransaction(
				eq(fromAccount),
				eq(referenceNumber),
				any(TransferEntity.class),
				eq(TransactionType.TRANSFER_OUT)
		);

		verify(transactionService).createTransferTransaction(
				eq(toAccount),
				eq(referenceNumber),
				any(TransferEntity.class),
				eq(TransactionType.TRANSFER_IN)
		);
	}


}
