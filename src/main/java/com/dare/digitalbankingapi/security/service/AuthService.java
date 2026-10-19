package com.dare.digitalbankingapi.security.service;

import com.dare.digitalbankingapi.security.jwt.JwtUtil;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.exceptions.UserIncorrectCredentialsException;
import com.dare.digitalbankingapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final UserService userService;
	private final JwtUtil jwtUtil;

	public String login(String email, String password) {
		UserEntity user = userService.getUserByEmail(email);

		boolean passwordMatches = passwordEncoder.matches(password,user.getPasswordHash());

		if (!passwordMatches) {
			log.info("Wrong password input for user with email: {}", email);
			throw new UserIncorrectCredentialsException();
		}

		log.info("Successful login for user with email: {}", email);
		return jwtUtil.generateToken(email,user.getUserRole(),user.getId());

	}

}
