package com.dare.digitalbankingapi.admin;

import com.dare.digitalbankingapi.user.dto.EmployeeUserRequest;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.entity.Role;
import com.dare.digitalbankingapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final UserService userService;

	@PostMapping("/employee")
	public ResponseEntity<UserResponse> createEmployeeUser(@RequestBody EmployeeUserRequest employeeUserRequest) {
		return ResponseEntity.ok(userService.createEmployeeUser(employeeUserRequest));
	}

	@PatchMapping("/users/{userId}/role")
	public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long userId, @RequestBody Role role) {
		return ResponseEntity.ok(userService.updateUserRole(userId,role));
	}
}
