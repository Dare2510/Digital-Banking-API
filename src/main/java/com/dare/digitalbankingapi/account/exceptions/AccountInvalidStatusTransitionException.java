package com.dare.digitalbankingapi.account.exceptions;

import com.dare.digitalbankingapi.account.entity.AccountStatus;

public class AccountInvalidStatusTransitionException extends RuntimeException {
	public AccountInvalidStatusTransitionException(AccountStatus oldStatus, AccountStatus newStatus) {

		super("Invalid status transition from " + oldStatus + " to " + newStatus);
	}
}
