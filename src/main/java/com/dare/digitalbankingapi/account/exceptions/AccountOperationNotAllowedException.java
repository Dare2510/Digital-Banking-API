package com.dare.digitalbankingapi.account.exceptions;

import com.dare.digitalbankingapi.account.entity.AccountStatus;

public class AccountOperationNotAllowedException extends RuntimeException {

	public AccountOperationNotAllowedException(Long accountId, AccountStatus status) {
		super("Operation not allowed for account with id " + accountId + ". Current status: " + status);
	}
}
