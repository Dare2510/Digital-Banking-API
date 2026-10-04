package com.dare.digitalbankingapi.user.exceptions;

public class EmailNotAvailableException extends RuntimeException {

	public EmailNotAvailableException(String email) {
		super("Email " + email + " is already taken");
	}
}
