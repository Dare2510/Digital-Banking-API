package com.dare.digitalbankingapi.user.exceptions;

public class UserProfileNotFoundException extends RuntimeException {
	public UserProfileNotFoundException(Long userId) {

		super("Could not find profile for user id " + userId);
	}
}
