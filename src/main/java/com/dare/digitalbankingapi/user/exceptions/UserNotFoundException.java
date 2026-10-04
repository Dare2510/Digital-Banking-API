package com.dare.digitalbankingapi.user.exceptions;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(Long userId) {
		super("User with id " + userId + " not found");
	}
}
