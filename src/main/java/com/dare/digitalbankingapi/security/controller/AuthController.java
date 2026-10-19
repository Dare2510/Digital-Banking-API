package com.dare.digitalbankingapi.security.controller;

import com.dare.digitalbankingapi.security.dto.LoginRequest;
import com.dare.digitalbankingapi.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/login")
public class AuthController {

	private final AuthService authService;

	@PostMapping
	public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
		return ResponseEntity.ok().body(authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
	}
}
