package com.dare.digitalbankingapi.user.exceptions;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(Long userId) {
		super("User with id " + userId + " not found");
	}

	public UserNotFoundException(String email) {
		super("User with email " + email + " not found");
	}
}
