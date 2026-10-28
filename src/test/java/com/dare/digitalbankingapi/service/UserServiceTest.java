package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.dto.EmployeeUserRequest;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.exceptions.EmailNotAvailableException;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
import com.dare.digitalbankingapi.user.repository.UserRepository;
import com.dare.digitalbankingapi.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	private UserService userService;

	private SortValidator sortValidator;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserProfileRepository userProfileRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Spy
	private ModelMapper modelMapper;

	private final String PASSWORD = "password";
	private final String EMAIL = "testEmail@mail.com";

	private final String HASHED_PASSWORD = "hashedPassword";
	private final Long USER_ID = 1L;
	private final UserRole CUSTOMER_USER_ROLE = UserRole.CUSTOMER;
	private final UserRole STAFF_USER_ROLE = UserRole.STAFF;

	@BeforeEach
	public void setUp() {
		userService = new UserService(userRepository, userProfileRepository,
				passwordEncoder, modelMapper, sortValidator);
	}

	//Helpers
	private CustomerUserRequest validCustomerRequest() {
		return new CustomerUserRequest(EMAIL, PASSWORD);
	}

	private EmployeeUserRequest validEmployeeRequest() {
		return new EmployeeUserRequest(EMAIL, PASSWORD, STAFF_USER_ROLE);
	}


	@Test
	public void createCustomerUser_whenRequestIsValid_returnsUserResponse() {
		CustomerUserRequest validRequest = validCustomerRequest();

		String normalizedMail = validRequest.getEmail().toLowerCase(Locale.ROOT).trim();

		when(userRepository.existsByEmail(normalizedMail)).thenReturn(false);
		when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(HASHED_PASSWORD);

		when(userRepository.saveAndFlush(any(UserEntity.class))).
				thenAnswer(
						i -> {
							UserEntity customer = i.getArgument(0);
							customer.setEmail(normalizedMail);
							customer.setId(USER_ID);
							return customer;

						});

		UserResponse customerUserResponse = userService.createCostumerUser(validRequest);

		assertEquals(normalizedMail, customerUserResponse.getEmail());
		assertEquals(USER_ID, customerUserResponse.getId());
		assertEquals(CUSTOMER_USER_ROLE, customerUserResponse.getUserRole());

		verify(userRepository, times(1)).saveAndFlush(any(UserEntity.class));
		verify(userRepository, times(1)).existsByEmail(normalizedMail);

	}

	@Test
	public void createCustomerUser_whenRequestEmailIsAlreadyUsed_throwsEmailNotAvailableException() {
		CustomerUserRequest emailUsedRequest = validCustomerRequest();

		String normalizedMail = emailUsedRequest.getEmail().toLowerCase(Locale.ROOT).trim();

		when(userRepository.existsByEmail(normalizedMail)).thenReturn(true);

		assertThatThrownBy(() -> userService.createCostumerUser(emailUsedRequest))
				.isInstanceOf(EmailNotAvailableException.class)
				.hasMessage("Email " + normalizedMail + " is already taken");

		verify(userRepository, times(1)).existsByEmail(normalizedMail);
		verify(userRepository, never()).saveAndFlush(any(UserEntity.class));

	}

	@Test
	public void createEmployeeUser_whenRequestIsValid_returnsUserResponse() {
		EmployeeUserRequest validRequest = validEmployeeRequest();
		String normalizedMail = validRequest.getEmail().toLowerCase(Locale.ROOT).trim();

		when(userRepository.existsByEmail(normalizedMail)).thenReturn(false);
		when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(HASHED_PASSWORD);

		when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(
				i -> {
					UserEntity employee = i.getArgument(0);
					employee.setEmail(normalizedMail);
					employee.setId(USER_ID);
					employee.setUserRole(STAFF_USER_ROLE);
					return employee;

				});
		UserResponse newEmployeeUserResponse = userService.createEmployeeUser(validRequest);

		assertEquals(normalizedMail, newEmployeeUserResponse.getEmail());
		assertEquals(USER_ID, newEmployeeUserResponse.getId());
		assertEquals(STAFF_USER_ROLE, newEmployeeUserResponse.getUserRole());

		verify(userRepository, times(1)).saveAndFlush(any(UserEntity.class));
		verify(userRepository, times(1)).existsByEmail(normalizedMail);
		verify(passwordEncoder, times(1)).encode(validRequest.getPassword());
	}


}






