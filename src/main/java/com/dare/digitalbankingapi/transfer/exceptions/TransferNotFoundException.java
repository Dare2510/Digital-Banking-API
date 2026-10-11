package com.dare.digitalbankingapi.transfer.exceptions;

public class TransferNotFoundException extends RuntimeException {
	public TransferNotFoundException(Long transferId) {

		super("Transfer with id " + transferId + " not found");
	}

	public TransferNotFoundException() {

		super("No transfers found");
	}
}
