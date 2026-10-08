package com.dare.digitalbankingapi.transfer.exceptions;

import com.dare.digitalbankingapi.account.entity.Currency;

import java.math.BigDecimal;

public class SufficientBalanceException extends RuntimeException {
	public SufficientBalanceException() {

		super("Account has sufficient balance for the requested transfer");
	}
}
