package com.dare.digitalbankingapi.transaction.exceptions;

import com.dare.digitalbankingapi.transaction.entity.TransactionType;

public class InvalidTransactionTypeException extends RuntimeException {
	public InvalidTransactionTypeException(TransactionType transactionType) {

		super(transactionType.name() + " is not supported, only Deposit and Withdraw are supported");
	}
}
