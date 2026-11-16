package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.exceptions.AccountOperationNotAllowedException;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

	private TransactionService transactionService;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private AccountService accountService;

	@Spy
	private SortValidator sortValidator;

	//Helper

	private AccountEntity setAccountValues() {

		AccountEntity accountEntity = new AccountEntity();

		accountEntity.setId(1L);
		accountEntity.setOwner(returnUserEntity());
		accountEntity.setAccountNumber("accountNumber");
		accountEntity.setCurrency(Currency.EUR);
		accountEntity.setStatus(AccountStatus.ACTIVE);
		accountEntity.setCreatedAt(LocalDateTime.now());

		return accountEntity;


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
		return new AuthenticatedUser(
				user.getId(),
				user.getEmail(),
				user.getUserRole()
		);
	}

	@BeforeEach
	void setUp() {
		transactionService = new TransactionService(transactionRepository, accountService, sortValidator);
	}

	@Test
	void depositTest_whenRequestIsValid_returnsTransactionResponse() {
		TransactionRequest depositRequest = new TransactionRequest(1L, BigDecimal.valueOf(500));
		AccountEntity account = setAccountValues();
		account.setBalance(BigDecimal.valueOf(500));

		when(accountService.getAccount(depositRequest.getAccountId())).thenReturn(account);

		when(transactionRepository.saveAndFlush(any(TransactionEntity.class)))
				.thenAnswer(invocation -> {
						TransactionEntity transaction = invocation.getArgument(0);
						transaction.setAccount(account);
						transaction.setAmount(depositRequest.getAmount());
						transaction.setType(TransactionType.DEPOSIT);
						transaction.setBalanceAfter(account.getBalance().add(depositRequest.getAmount()));
						transaction.setCreatedAt(LocalDateTime.now());
						transaction.setId(1L);

						account.setBalance(account.getBalance().add(depositRequest.getAmount()));

						return transaction;
				});

		TransactionResponse depositResponse = transactionService.deposit(depositRequest);

		assertEquals(depositRequest.getAccountId(),depositResponse.getFromAccountId());
		assertEquals(depositRequest.getAmount(),depositResponse.getAmount());
		assertEquals(account.getBalance(), depositResponse.getBalanceAfter());

		verify(accountService,times(1)).getAccount(1L);
		verify(accountService,times(1)).updateBalance(any(),any(),any());
		verify(transactionRepository,times(1)).saveAndFlush(any(TransactionEntity.class));


	}

	@Test
	public void depositTest_whenAccountStatusIsNotValid_throwsAccountOperationNotAllowedException() {
		TransactionRequest depositRequest = new TransactionRequest(1L, BigDecimal.valueOf(500));
		AccountEntity account = setAccountValues();
		account.setBalance(BigDecimal.valueOf(500));
		account.setStatus(AccountStatus.CLOSED);

		when(accountService.getAccount(depositRequest.getAccountId())).thenReturn(account);

		doThrow(new AccountOperationNotAllowedException(account.getId(), account.getStatus()))
				.when(accountService).checkAccountStatus(account);

		assertThatThrownBy( () ->  transactionService.deposit(depositRequest))
		.isInstanceOf(AccountOperationNotAllowedException.class)
				.hasMessage("Operation not allowed for account with id " + depositRequest.getAccountId() +
						". Current status: " + account.getStatus());

		verify(accountService,times(1)).getAccount(depositRequest.getAccountId());
		verify(transactionRepository,never()).saveAndFlush(any(TransactionEntity.class));

		assertEquals(BigDecimal.valueOf(500), account.getBalance());

	}

	@Test
	public void withdrawTest_whenRequestIsValid_returnsTransactionResponse() {
		TransactionRequest withdrawalRequest = new TransactionRequest(1L, BigDecimal.valueOf(1000));
		AccountEntity account = setAccountValues();
		AuthenticatedUser authenticatedCostumer = returnAuthenticatedUser(account.getOwner());
		account.setBalance(BigDecimal.valueOf(5000));

		when(accountService.getAccount(withdrawalRequest.getAccountId())).thenReturn(account);
		when(accountService.userIsOwner(account,authenticatedCostumer)).thenReturn(true);

		when(transactionRepository.saveAndFlush(any(TransactionEntity.class)))
				.thenAnswer(invocation -> {
					TransactionEntity transaction = invocation.getArgument(0);
					transaction.setId(1L);
					transaction.setAccount(account);
					transaction.setAmount(withdrawalRequest.getAmount());
					transaction.setType(TransactionType.WITHDRAW);
					transaction.setBalanceAfter(account.getBalance().subtract(withdrawalRequest.getAmount()));
					account.setBalance(account.getBalance().subtract(withdrawalRequest.getAmount()));
					return transaction;
				});

		TransactionResponse response = transactionService.withdraw(authenticatedCostumer, withdrawalRequest);

		assertEquals(withdrawalRequest.getAccountId(),response.getFromAccountId());
		assertEquals(withdrawalRequest.getAmount(),response.getAmount());
		assertEquals(account.getBalance(), response.getBalanceAfter());

		verify(accountService,times(1)).getAccount(withdrawalRequest.getAccountId());
		verify(accountService,times(1)).userIsOwner(account,authenticatedCostumer);
		verify(accountService,times(1)).checkAccountStatus(account);
		verify(accountService,times(1)).updateBalance(any(),any(),any());
		verify(transactionRepository,times(1)).saveAndFlush(any(TransactionEntity.class));
	}

	@Test
	public void withdrawTest_whenBalanceIsInsufficient_throwsInsufficientBalanceException() {
		TransactionRequest withdrawalRequest = new TransactionRequest(1L, BigDecimal.valueOf(1000));
		AccountEntity account = setAccountValues();
		account.setBalance(BigDecimal.valueOf(500));

		AuthenticatedUser authenticatedCostumer = returnAuthenticatedUser(account.getOwner());

		when(accountService.getAccount(withdrawalRequest.getAccountId())).thenReturn(account);
		when(accountService.userIsOwner(account,authenticatedCostumer)).thenReturn(true);

		assertThatThrownBy( () -> transactionService.withdraw(authenticatedCostumer, withdrawalRequest))
		.isInstanceOf(InsufficientBalanceException.class)
				.hasMessage("Account has insufficient balance for the requested withdrawal");

		assertEquals(BigDecimal.valueOf(500), account.getBalance());

		verify(accountService,times(1)).getAccount(withdrawalRequest.getAccountId());
		verify(accountService,times(1)).userIsOwner(account,authenticatedCostumer);
		verify(accountService,times(1)).checkAccountStatus(account);
		verify(accountService,never()).updateBalance(any(),any(),any());
		verify(transactionRepository,never()).saveAndFlush(any(TransactionEntity.class));


	}
}
