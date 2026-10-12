package com.dare.digitalbankingapi.account.exceptions;

import com.dare.digitalbankingapi.account.entity.AccountStatus;

public class AccountOperationNotAllowedException extends RuntimeException {
	public AccountOperationNotAllowedException(Long accountId, AccountStatus status) {

		super("Invalid account status for transaction with id: " + accountId + " and status: " + status);
	}
}
