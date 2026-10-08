package com.dare.digitalbankingapi.transaction.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor

public class TransactionService {

	private final TransactionRepository transactionRepository;

	public TransactionEntity createTransaction(AccountEntity account, String reference, TransferEntity transfer, TransactionType transactionType) {
		BigDecimal amount = transfer.getAmount();
		BigDecimal balanceAfter = calculateBalanceAfter(account, amount, transactionType);

		TransactionEntity transaction = new TransactionEntity(
				account,
				transfer.getAmount(),
				reference,
				transactionType,
				balanceAfter,
				transfer

		);

		transactionRepository.saveAndFlush(transaction);
		return transaction;

	}

	private BigDecimal calculateBalanceAfter(AccountEntity account, BigDecimal amount, TransactionType transactionType) {
		boolean incoming = transactionType == TransactionType.DEPOSIT || transactionType == TransactionType.TRANSFER_IN;

		if (incoming) {
			return account.getBalance().add(amount);
		} else {
			return account.getBalance().subtract(amount);
		}
	}
}
