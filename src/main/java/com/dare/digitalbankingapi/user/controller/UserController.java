package com.dare.digitalbankingapi.user.controller;

import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.dto.UserRequest;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/register")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserProfileService userProfileService;

	@PostMapping
	public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
		return ResponseEntity.ok(userService.createCostumerUser(userRequest));
	}

	@PostMapping("/profile")
	public ResponseEntity<UserProfileResponse> createProfile(@RequestBody UserProfileRequest userProfileRequest) {
		return ResponseEntity.ok(userProfileService.createUserProfile(userProfileRequest));
	}
}
