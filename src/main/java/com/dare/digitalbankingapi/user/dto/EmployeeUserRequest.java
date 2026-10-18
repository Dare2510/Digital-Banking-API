package com.dare.digitalbankingapi.user.dto;

import com.dare.digitalbankingapi.user.entity.UserRole;
import jakarta.validation.constraints.*;
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

	@NotNull(message = "Role is required")
	private UserRole userRole;

	@AssertTrue(message = "Role must be STAFF or ADMIN")
	public boolean isEmployeeRoleValid() {
		return userRole == null || userRole == UserRole.STAFF || userRole == UserRole.ADMIN;
	}
}
