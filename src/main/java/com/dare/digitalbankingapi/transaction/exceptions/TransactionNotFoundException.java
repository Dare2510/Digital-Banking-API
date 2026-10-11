package com.dare.digitalbankingapi.transaction.exceptions;

public class TransactionNotFoundException extends RuntimeException {
	public TransactionNotFoundException(Long transferId) {

		super("Transaction for transfer Id " + transferId + " not found");
	}
}
