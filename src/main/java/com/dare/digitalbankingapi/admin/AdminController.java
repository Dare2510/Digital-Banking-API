package com.dare.digitalbankingapi.admin;

import com.dare.digitalbankingapi.user.dto.EmployeeUserRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final UserService userService;
	private final UserProfileService userProfileService;

	@PostMapping("/employee")
	public ResponseEntity<UserResponse> createEmployeeUser(@Valid @RequestBody EmployeeUserRequest employeeUserRequest) {
		return ResponseEntity.ok(userService.createEmployeeUser(employeeUserRequest));
	}

	@PostMapping("/employee/{userId}/profile")
	public ResponseEntity<UserProfileResponse> createEmployeeProfile(@PathVariable Long userId,
																	 @Valid @RequestBody UserProfileRequest userProfileRequest) {
		return ResponseEntity.ok(userProfileService.createUserProfile(userId, userProfileRequest));
	}

	@PatchMapping("/users/{userId}/role")
	public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long userId, @RequestBody UserRole userRole) {
		return ResponseEntity.ok(userService.updateUserRole(userId, userRole));
	}
}
