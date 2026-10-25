package com.dare.digitalbankingapi.security.service;

import com.dare.digitalbankingapi.security.jwt.JwtUtil;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.exceptions.UserIncorrectCredentialsException;
import com.dare.digitalbankingapi.user.exceptions.UserNotFoundException;
import com.dare.digitalbankingapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final UserService userService;
	private final JwtUtil jwtUtil;

	public String login(String email, String password) {

		String normalizedEmail = email.trim()
				.toLowerCase(Locale.ROOT);

		UserEntity user;

		try {
			user = userService.getUserByEmail(normalizedEmail);
		} catch (UserNotFoundException ex) {
			throw new UserIncorrectCredentialsException();
		}

		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new UserIncorrectCredentialsException();
		}

		log.info("Successful login for user with id {}", user.getId());

		return jwtUtil.generateToken(
				user.getEmail(),
				user.getUserRole(),
				user.getId()
		);
	}

}
