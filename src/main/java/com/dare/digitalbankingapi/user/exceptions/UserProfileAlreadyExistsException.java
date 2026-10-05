package com.dare.digitalbankingapi.user.exceptions;

public class UserProfileAlreadyExistsException extends RuntimeException {
	public UserProfileAlreadyExistsException(Long userId) {

		super("UserProfile already exists for user id: " + userId);
	}
}
