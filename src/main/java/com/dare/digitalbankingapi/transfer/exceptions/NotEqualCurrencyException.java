package com.dare.digitalbankingapi.transfer.exceptions;

public class NotEqualCurrencyException extends RuntimeException {
	public NotEqualCurrencyException() {
		super("Can not transfer, currencies are not equal");
	}
}
