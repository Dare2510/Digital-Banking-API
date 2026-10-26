package com.dare.digitalbankingapi.user.controller;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth/register")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserProfileService userProfileService;

	@PostMapping
	public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody CustomerUserRequest customerUserRequest) {
		return ResponseEntity.ok(userService.createCostumerUser(customerUserRequest));
	}

	@PostMapping("/profile")
	public ResponseEntity<UserProfileResponse> createProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
															 @Valid @RequestBody UserProfileRequest userProfileRequest) {
		return ResponseEntity.ok(userProfileService.createUserProfile(authenticatedUser, userProfileRequest));
	}

	@PatchMapping("/profile/update")
	public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
															 @Valid @RequestBody UserProfileRequest userProfileRequest) {
		return ResponseEntity.ok(userProfileService.updateUserProfile(authenticatedUser, userProfileRequest));
	}
}
