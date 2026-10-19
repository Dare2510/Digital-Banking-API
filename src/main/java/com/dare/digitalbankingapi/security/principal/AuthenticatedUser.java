package com.dare.digitalbankingapi.security.principal;

import com.dare.digitalbankingapi.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class AuthenticatedUser {

	private Long userId;
	private String email;
	private UserRole role;
}
