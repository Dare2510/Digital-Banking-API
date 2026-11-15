package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
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

	private UserEntity returnToUserEntity() {
		UserEntity customer = new UserEntity(
				"testEmail@mail.com",
				"passwordHash",
				UserRole.CUSTOMER
		);

		customer.setId(2L);
		return customer;

	}

	@BeforeEach
	void setUp() {
		transactionService = new TransactionService(transactionRepository, accountService, sortValidator);
	}

	@Test
	void depositTest_whenRequestIsValid_returnsTransactionResponse() {
		TransactionRequest request = new TransactionRequest(1L, BigDecimal.valueOf(500));
		UserEntity loggedInCustomer = returnToUserEntity();
		AccountEntity account = setAccountValues(1L, "accountNumber", loggedInCustomer);
		account.setBalance(BigDecimal.valueOf(500));

		when(accountService.getAccount(account.getId())).thenReturn(account);

		when(transactionRepository.saveAndFlush(any(TransactionEntity.class)))
				.thenAnswer(invocation -> {
						TransactionEntity transaction = invocation.getArgument(0);
						transaction.setAccount(account);
						transaction.setAmount(request.getAmount());
						transaction.setType(TransactionType.DEPOSIT);
						transaction.setBalanceAfter(account.getBalance().add(request.getAmount()));
						transaction.setCreatedAt(LocalDateTime.now());
						transaction.setId(1L);

						account.setBalance(account.getBalance().add(request.getAmount()));

						return transaction;
				});

		TransactionResponse response = transactionService.deposit(request);

		assertEquals(request.getAccountId(),response.getFromAccountId());
		assertEquals(request.getAmount(),response.getAmount());
		assertEquals(account.getBalance(), response.getBalanceAfter());

		verify(accountService,times(1)).getAccount(1L);
		verify(accountService,times(1)).updateBalance(any(),any(),any());
		verify(transactionRepository,times(1)).saveAndFlush(any(TransactionEntity.class));


	}
}
