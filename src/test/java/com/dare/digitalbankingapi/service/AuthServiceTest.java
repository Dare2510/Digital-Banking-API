package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.security.dto.LoginRequest;
import com.dare.digitalbankingapi.security.jwt.JwtUtil;
import com.dare.digitalbankingapi.security.service.AuthService;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.exceptions.UserIncorrectCredentialsException;
import com.dare.digitalbankingapi.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	private AuthService authService;

	@Mock
	private UserService userService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private PasswordEncoder passwordEncoder;

	//Fields

	private final String CORRECT_MAIL = "correct@mail.com";

	//Helper
	private UserEntity returnUser() {
		String HASHED_PASSWORD = "passwordHash";
		UserEntity customer = new UserEntity
				(CORRECT_MAIL.trim().toLowerCase(Locale.ROOT), HASHED_PASSWORD, UserRole.CUSTOMER);

		Long USER_ID = 1L;
		customer.setId(USER_ID);

		return customer;
	}

	private LoginRequest successfulLoginRequest() {
		String CORRECT_PASSWORD = "CorrectPassword";
		return new LoginRequest(CORRECT_MAIL.trim().toLowerCase(Locale.ROOT), CORRECT_PASSWORD);
	}

	private LoginRequest incorrectPasswordLoginRequest() {
		String INCORRECT_PASSWORD = "IncorrectPassword";
		return new LoginRequest(CORRECT_MAIL.trim().toLowerCase(Locale.ROOT), INCORRECT_PASSWORD);
	}


	@BeforeEach
	public void setUp() {
		authService = new AuthService(passwordEncoder, userService, jwtUtil);
	}

	@Test
	public void login_whenRequestIsValid_returnsJwtToken() {
		LoginRequest validLoginRequest = successfulLoginRequest();
		UserEntity user = returnUser();

		when(userService.getUserByEmail(validLoginRequest.getEmail())).thenReturn(user);
		when(passwordEncoder.matches(validLoginRequest.getPassword(), user.getPasswordHash())).thenReturn(true);

		authService.login(validLoginRequest.getEmail(), validLoginRequest.getPassword());

		verify(userService, times(1)).getUserByEmail(validLoginRequest.getEmail());
		verify(passwordEncoder, times(1)).matches(validLoginRequest.getPassword(), user.getPasswordHash());
		verify(jwtUtil, times(1)).generateToken(user.getEmail(), user.getUserRole(), user.getId());

	}

	@Test
	public void login_whenPasswordIsIncorrect_throwsUserIncorrectCredentialsException() {
		LoginRequest incorrectPasswordLoginRequest = incorrectPasswordLoginRequest();
		UserEntity user = returnUser();

		when(userService.getUserByEmail(incorrectPasswordLoginRequest.getEmail())).thenReturn(user);
		when(passwordEncoder.matches(incorrectPasswordLoginRequest.getPassword(), user.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> authService.login(incorrectPasswordLoginRequest.getEmail(), incorrectPasswordLoginRequest.getPassword()))
				.isInstanceOf(UserIncorrectCredentialsException.class)
				.hasMessage("Password and/or Email input is incorrect");

		verify(jwtUtil, never()).generateToken(user.getEmail(), user.getUserRole(), user.getId());
		verify(userService, times(1)).getUserByEmail(incorrectPasswordLoginRequest.getEmail());
		verify(passwordEncoder, times(1)).matches(incorrectPasswordLoginRequest.getPassword(), user.getPasswordHash());

	}
}
