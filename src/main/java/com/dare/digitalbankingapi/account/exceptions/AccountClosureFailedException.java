package com.dare.digitalbankingapi.account.exceptions;

import com.dare.digitalbankingapi.account.entity.AccountStatus;

public class AccountClosureFailedException extends RuntimeException {

	public AccountClosureFailedException(Long accountId, AccountStatus accountStatus) {
		super("Account with id " + accountId + " cannot be closed, wrong status: " + accountStatus);
	}
}
