package com.dare.digitalbankingapi.user.exceptions;

public class UserProfileUpdateFailedNoProfileExists extends RuntimeException {

	public UserProfileUpdateFailedNoProfileExists(Long userId) {
		super("Profile update failed for user id: " + userId +
				"no profile exists");
	}
}
