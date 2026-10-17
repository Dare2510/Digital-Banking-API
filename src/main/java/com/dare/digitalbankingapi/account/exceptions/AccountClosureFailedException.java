package com.dare.digitalbankingapi.account.exceptions;

import com.dare.digitalbankingapi.account.entity.AccountStatus;

import java.math.BigDecimal;

public class AccountClosureFailedException extends RuntimeException {

	public AccountClosureFailedException(Long accountId, AccountStatus accountStatus, BigDecimal balance) {
		super("Account with id " + accountId
				+ " cannot be closed. Current status: " + accountStatus
				+ ", current balance: " + balance
				+ ". Account must be ACTIVE and have zero balance.");
	}
}
