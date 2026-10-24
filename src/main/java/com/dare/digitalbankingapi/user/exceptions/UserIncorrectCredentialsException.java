package com.dare.digitalbankingapi.user.exceptions;

public class UserIncorrectCredentialsException extends RuntimeException {
	public UserIncorrectCredentialsException() {

		super("Password and/or Email input is incorrect");
	}
}
