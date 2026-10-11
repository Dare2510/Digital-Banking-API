package com.dare.digitalbankingapi.transaction.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountService accountService;

	@Transactional
	public TransactionResponse deposit(TransactionRequest request) {

		AccountEntity account = accountService.getAccount(request.getAccountId());

		BigDecimal amount = request.getAmount();

		accountService.updateBalance(account, amount, TransactionType.DEPOSIT);

		TransactionEntity transaction =
				new TransactionEntity(
						account,
						amount,
						TransactionType.DEPOSIT.name(),
						TransactionType.DEPOSIT,
						account.getBalance()
				);

		transactionRepository.saveAndFlush(transaction);

		return buildResponse(account, amount);
	}

	@Transactional
	public TransactionResponse withdraw(TransactionRequest request) {

		AccountEntity account = accountService.getAccount(request.getAccountId());

		BigDecimal amount = request.getAmount();

		BigDecimal balanceAfter = account.getBalance().subtract(amount);

		if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientBalanceException(
					TransactionType.WITHDRAW
			);
		}

		accountService.updateBalance(account, amount, TransactionType.WITHDRAW);

		TransactionEntity transaction =
				new TransactionEntity(
						account,
						amount,
						TransactionType.WITHDRAW.name(),
						TransactionType.WITHDRAW,
						account.getBalance()
				);

		transactionRepository.saveAndFlush(transaction);

		return buildResponse(account, amount);
	}

	public void createTransferTransaction(AccountEntity account, String reference,
										  TransferEntity transfer, TransactionType transactionType) {

		TransactionEntity transaction =
				new TransactionEntity(
						account,
						transfer.getAmount(),
						reference,
						transactionType,
						account.getBalance(),
						transfer
				);

		transactionRepository.saveAndFlush(transaction);
	}

	private TransactionResponse buildResponse(AccountEntity account, BigDecimal amount) {

		return TransactionResponse.builder()
				.fromAccountId(account.getId())
				.amount(amount)
				.balanceAfter(account.getBalance())
				.build();
	}
}