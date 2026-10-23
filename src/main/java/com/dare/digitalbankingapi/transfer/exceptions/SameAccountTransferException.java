package com.dare.digitalbankingapi.transfer.exceptions;

public class SameAccountTransferException extends RuntimeException {

	public SameAccountTransferException() {
		super("Transfer to the same account is not allowed");
	}
}
