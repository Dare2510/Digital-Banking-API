package com.dare.digitalbankingapi.account.exceptions;

import com.dare.digitalbankingapi.account.entity.AccountStatus;

public class AccountOperationNotAllowedException extends RuntimeException {
	public AccountOperationNotAllowedException(Long accountId, AccountStatus status) {

		super("Account id " + accountId + " has status " + status + ".Invalid status for operations");
	}
}
