
package com.dare.digitalbankingapi.user.service;

import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.user.dto.CustomerUserRequest;
import com.dare.digitalbankingapi.user.dto.EmployeeUserRequest;
import com.dare.digitalbankingapi.user.dto.UserDetailsResponse;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.entity.Role;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.exceptions.EmailNotAvailableException;
import com.dare.digitalbankingapi.user.exceptions.UserNotFoundException;
import com.dare.digitalbankingapi.user.exceptions.UserProfileNotFoundException;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
import com.dare.digitalbankingapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final SortValidator sortValidator;

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
			"id",
			"email",
			"role"
	);

	@Transactional
	public UserResponse createCostumerUser(CustomerUserRequest customerUserRequest) {
		String email = normalizeEmail(customerUserRequest.getEmail());
		validateUserRequest(email);

		String password = customerUserRequest.getPassword();
		Role customerRole = Role.CUSTOMER;

		return createUserAndGiveResponse(email, password, customerRole);
	}

	@Transactional
	public UserResponse createEmployeeUser(EmployeeUserRequest employeeUserRequest) {
		String email = normalizeEmail(employeeUserRequest.getEmail());
		validateUserRequest(email);

		String password = employeeUserRequest.getPassword();
		Role staffRole = employeeUserRequest.getRole();

		return createUserAndGiveResponse(email, password, staffRole);
	}

	public UserResponse updateUserRole(Long userId, Role role) {
		UserEntity userToUpdate = getUserById(userId);
		userToUpdate.setRole(role);

		userRepository.save(userToUpdate);

		return modelMapper.map(userToUpdate, UserResponse.class);
	}

	public Page<UserDetailsResponse> getPageOfUsersWithDetails(Pageable pageable) {
		sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

		return userRepository.findAll(pageable)
				.map(user -> {
					log.info("Getting Page of Users with details)");
					return buildUserDetailResponse(user);
				});
	}

	// Helper Methods

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private void validateUserRequest(String email) {
		if (userRepository.existsByEmail(email)) {
			log.info("Email {} is already taken", email);
			throw new EmailNotAvailableException(email);
		}
	}

	public UserEntity getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(
						() -> {
							log.info("User with id {} was not found", userId);
							return new UserNotFoundException(userId);
						}
				);
	}

	private UserDetailsResponse buildUserDetailResponse(UserEntity user) {
		UserProfileEntity profile = userProfileRepository
				.findByUserId(user.getId())
				.orElseThrow(
						() -> new UserProfileNotFoundException(user.getId())
				);

		return new UserDetailsResponse(
				user.getId(),
				user.getEmail(),
				user.getRole(),
				profile.getName(),
				profile.getSurname(),
				profile.getStreet(),
				profile.getHouseNumber(),
				profile.getCity(),
				profile.getZipCode(),
				profile.getCountry()
		);
	}

	private UserResponse createUserAndGiveResponse(String email, String password, Role role) {
		try {
			UserEntity user = new UserEntity(
					email,
					passwordEncoder.encode(password),
					role
			);

			userRepository.saveAndFlush(user);

			log.info("Created User with id {} and role {}",
					user.getId(), user.getRole());

			return modelMapper.map(user, UserResponse.class);

		} catch (DataIntegrityViolationException ex) {
			log.info("Could not create user with role {} and email {} is already taken",
					role, email);
			throw new EmailNotAvailableException(email);
		}
	}
}
