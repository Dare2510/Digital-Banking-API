package com.dare.digitalbankingapi.transaction.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.exceptions.InvalidTransactionTypeException;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor

public class TransactionService {

	private final TransactionRepository transactionRepository;

	public void createTransaction(AccountEntity account, String reference, TransferEntity transfer, TransactionType transactionType) {
		BigDecimal amount = transfer.getAmount();
		BigDecimal balanceAfter = calculateBalanceAfter(account, amount, transactionType);

		if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientBalanceException(transactionType);
		}

		TransactionEntity transaction = new TransactionEntity(
				account,
				transfer.getAmount(),
				reference,
				transactionType,
				balanceAfter,
				transfer

		);

		transactionRepository.saveAndFlush(transaction);

	}

	private BigDecimal calculateBalanceAfter(AccountEntity account, BigDecimal amount, TransactionType transactionType) {
		boolean incoming = transactionType == TransactionType.DEPOSIT || transactionType == TransactionType.TRANSFER_IN;

		if (incoming) {
			return account.getBalance().add(amount);
		} else {
			return account.getBalance().subtract(amount);
		}
	}

	public void transactionTypeIsValidForDepositAndWithdrawal(TransactionType transactionType) {
		boolean typeIsValid = transactionType == TransactionType.DEPOSIT || transactionType == TransactionType.WITHDRAW;

		if (!typeIsValid) {
			throw new InvalidTransactionTypeException(transactionType);
		}
	}
}
