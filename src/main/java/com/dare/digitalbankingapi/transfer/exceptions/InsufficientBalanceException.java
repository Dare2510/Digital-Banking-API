package com.dare.digitalbankingapi.transfer.exceptions;

import com.dare.digitalbankingapi.transaction.entity.TransactionType;

public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException() {

		super("Account has sufficient balance for the requested transfer");
	}

	public InsufficientBalanceException(TransactionType transactionType) {

		super("Account has sufficient balance for the requested withdrawal");
	}
}
