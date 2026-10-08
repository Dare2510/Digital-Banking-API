package com.dare.digitalbankingapi.transfer.exceptions;

public class SufficientBalanceException extends RuntimeException {
	public SufficientBalanceException() {

		super("Account has sufficient balance for the requested transfer");
	}
}
