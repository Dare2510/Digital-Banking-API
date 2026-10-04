package com.dare.digitalbankingapi.user.dto;

import com.dare.digitalbankingapi.user.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

	private final Long id;
	private final String email;
	private final Role role;
}
