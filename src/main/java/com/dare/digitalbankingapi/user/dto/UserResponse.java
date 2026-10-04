package com.dare.digitalbankingapi.user.dto;

import com.dare.digitalbankingapi.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private Long id;
	private String email;
	private Role role;
}
