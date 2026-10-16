package com.dare.digitalbankingapi.transaction.exceptions;

public class SameAccountTransferException extends RuntimeException {

	public SameAccountTransferException() {
		super("Transfer to the same account is not allowed");
	}
}
