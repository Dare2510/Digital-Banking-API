package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.exceptions.AccountOperationNotAllowedException;
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
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import com.dare.digitalbankingapi.transfer.exceptions.SameAccountTransferException;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
	private UserEntity returnFromUserEntity() {
		UserEntity customer = new UserEntity(
				"testEmail@mail.com",
				"passwordHash",
				UserRole.CUSTOMER
		);

		customer.setId(1L);
		return customer;

	}

	private UserEntity returnToUserEntity() {
		UserEntity customer = new UserEntity(
				"testEmail@mail.com",
				"passwordHash",
				UserRole.CUSTOMER
		);

		customer.setId(2L);
		return customer;

	}

	private AuthenticatedUser returnAuthenticatedUser(UserEntity user) {
		return new AuthenticatedUser(user.getId(), user.getEmail(), user.getUserRole());
	}

	private AccountEntity setAccountValues(Long accountId, String accountNumber,
								 	 UserEntity userEntity) {

		AccountEntity accountEntity = new AccountEntity();

		accountEntity.setId(accountId);
		accountEntity.setOwner(userEntity);
		accountEntity.setAccountNumber(accountNumber);
		accountEntity.setCurrency(Currency.EUR);
		accountEntity.setStatus(AccountStatus.ACTIVE);
		accountEntity.setCreatedAt(LocalDateTime.now());

		return accountEntity;


	}


	@Test
	public void createTransferBetweenAccounts_whenRequestIsValid_returnsTransferResponse() {

		Long fromAccountId = 1L;
		Long toAccountId = 2L;

		BigDecimal amount = new BigDecimal("100");
		String referenceNumber = "referenceNumber";

		UserEntity fromUser = returnFromUserEntity();
		UserEntity toUser = returnToUserEntity();

		AccountEntity fromAccount = setAccountValues(fromAccountId,"fromAccountNumber", fromUser);
		fromAccount.setBalance(new BigDecimal("1000"));

		AccountEntity toAccount = setAccountValues(toAccountId,"toAccountNumber",toUser);
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

	@Test
	public void createTransferBetweenAccountsTest_whenBalanceIsInsufficient_throwsInsufficientBalanceException() {

		Long fromAccountId = 1L;
		Long toAccountId = 2L;

		BigDecimal amount = new BigDecimal("2000");
		String referenceNumber = "referenceNumber";

		UserEntity fromUser = returnFromUserEntity();
		UserEntity toUser = returnToUserEntity();

		AccountEntity fromAccount = setAccountValues(fromAccountId,"fromAccountNumber", fromUser);
		fromAccount.setBalance(new BigDecimal("1000"));

		AccountEntity toAccount = setAccountValues(toAccountId,"toAccountNumber",toUser);
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

		assertThatThrownBy( () -> transferService.createTransferBetweenAccounts(loggedInUser, transferRequest))
				.isInstanceOf(InsufficientBalanceException.class)
				.hasMessage("Account has insufficient balance for the requested transfer");

		assertEquals(BigDecimal.valueOf(Long.parseLong("1000")), fromAccount.getBalance());
		assertEquals(BigDecimal.valueOf(Long.parseLong("100")), toAccount.getBalance());

		verify(accountService).userIsOwner(fromAccount, loggedInUser);
		verify(accountService).checkAccountStatus(fromAccount);
		verify(accountService).checkAccountStatus(toAccount);
		verify(accountService,never()).updateBalance(any(), any(), any());
		verify(transferRepository,never()).saveAndFlush(any(TransferEntity.class));
		verify(transactionService,never()).createTransferTransaction(any(),any(),any(),any());



	}

	@Test
	public void createTransferBetweenAccountsTest_whenAccountIsInactive_throwsAccountOperationNotAllowedException() {
		Long fromAccountId = 1L;
		Long toAccountId = 2L;

		BigDecimal amount = new BigDecimal("100");
		String referenceNumber = "referenceNumber";

		UserEntity fromUser = returnFromUserEntity();
		UserEntity toUser = returnToUserEntity();

		AccountEntity fromAccount = setAccountValues(fromAccountId,"fromAccountNumber", fromUser);
		fromAccount.setBalance(new BigDecimal("1000"));

		AccountEntity toAccount = setAccountValues(toAccountId,"toAccountNumber",toUser);
		toAccount.setBalance(new BigDecimal("100"));
		toAccount.setStatus(AccountStatus.CLOSED);

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

		doNothing().when(accountService).checkAccountStatus(fromAccount);

		doThrow(new AccountOperationNotAllowedException(toAccountId, AccountStatus.CLOSED))
				.when(accountService).checkAccountStatus(toAccount);

		assertThatThrownBy(() -> transferService.createTransferBetweenAccounts(loggedInUser, transferRequest))
				.isInstanceOf(AccountOperationNotAllowedException.class)
				.hasMessage("Operation not allowed for account with id " + toAccountId + ". Current status: " + AccountStatus.CLOSED);

		assertEquals(BigDecimal.valueOf(Long.parseLong("1000")), fromAccount.getBalance());
		assertEquals(BigDecimal.valueOf(Long.parseLong("100")), toAccount.getBalance());

		verify(accountService).userIsOwner(fromAccount, loggedInUser);
		verify(accountService).checkAccountStatus(fromAccount);
		verify(accountService).checkAccountStatus(toAccount);
		verify(accountService,never()).updateBalance(any(), any(), any());
		verify(transferRepository,never()).saveAndFlush(any(TransferEntity.class));
		verify(transactionService,never()).createTransferTransaction(any(),any(),any(),any());


	}

	@Test
	public void createTransferBetweenAccountsTest_whenTransferBetweenSameAccountsIsRequested_throwsSameAccountTransferException() {
		Long accountId = 1L;

		BigDecimal amount = new BigDecimal("100");
		String referenceNumber = "referenceNumber";

		UserEntity user = returnFromUserEntity();

		AccountEntity account = setAccountValues(accountId,"fromAccountNumber", user);
		account.setBalance(new BigDecimal("1000"));

		AuthenticatedUser loggedInUser = returnAuthenticatedUser(user);

		TransferRequest transferRequest = new TransferRequest(
				accountId,
				accountId,
				amount,
				referenceNumber
		);


		when(accountService.getAccount(accountId)).thenReturn(account);
		when(accountService.userIsOwner(account, loggedInUser)).thenReturn(true);

		assertThatThrownBy(() -> transferService.createTransferBetweenAccounts(loggedInUser, transferRequest))
				.isInstanceOf(SameAccountTransferException.class)
				.hasMessage("Transfer to the same account is not allowed");

		assertEquals(BigDecimal.valueOf(Long.parseLong("1000")), account.getBalance());

		verify(accountService,times(2)).getAccount(accountId);
		verify(accountService).userIsOwner(account, loggedInUser);
		verify(accountService,never()).checkAccountStatus(account);
		verify(accountService,never()).updateBalance(any(), any(), any());
		verify(transferRepository,never()).saveAndFlush(any(TransferEntity.class));
		verify(transactionService,never()).createTransferTransaction(any(),any(),any(),any());


	}

	@Test
	public void viewTransferDetails_whenTransferIsFoundAndUserIsOwner_returnsTransferDetails() {
		Long transferId = 1L;

		Long fromAccountId = 1L;
		UserEntity user = returnFromUserEntity();
		AuthenticatedUser loggedInUser = returnAuthenticatedUser(user);
		AccountEntity fromAccount = setAccountValues(fromAccountId,"fromAccountNumber",user);

		UserEntity toUser = returnFromUserEntity();
		toUser.setId(2L);

		AccountEntity toAccount = setAccountValues(2L,"toAccountNumber",toUser);
		BigDecimal amount = new BigDecimal("1000");

		TransferEntity transfer = new TransferEntity(fromAccount,toAccount,amount,Currency.EUR);
		transfer.setId(transferId);

		TransactionEntity transaction = new TransactionEntity(
				fromAccount,
				amount,
				"reference",
				TransactionType.TRANSFER_OUT,
				BigDecimal.valueOf(500),
				transfer
				);

		when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
		when(transactionService.getTransactionByTransferId(transferId)).thenReturn(transaction);

		TransferResponse response = transferService.viewTransferDetails(loggedInUser,transferId);

		assertEquals(fromAccountId,response.getFromAccountId());
		assertEquals(toAccount.getId(),response.getToAccountId());
		assertEquals(transfer.getId(),response.getTransferId());
		assertEquals(transfer.getAmount(), response.getAmount());
		assertEquals(transaction.getReference(),response.getReference());

		verify(transferRepository).findById(transferId);
		verify(transactionService).getTransactionByTransferId(transferId);
	}


}
