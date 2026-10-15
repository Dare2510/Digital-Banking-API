package com.dare.digitalbankingapi.user.dto;

import com.dare.digitalbankingapi.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeUserRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email address")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
	private String password;

	private Role role;
}
