package com.dare.digitalbankingapi.transaction.exceptions;

public class TransactionNotFoundException extends RuntimeException {
	public TransactionNotFoundException(Long id) {

		super("Transaction for transfer Id " + id + " not found");
	}
}
