package com.dare.digitalbankingapi.user.service;

import com.dare.digitalbankingapi.user.dto.UserRequest;
import com.dare.digitalbankingapi.user.dto.UserResponse;
import com.dare.digitalbankingapi.user.entity.Role;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.exceptions.EmailNotAvailableException;
import com.dare.digitalbankingapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;


	public UserResponse createCostumerUser(UserRequest userRequest) {
		String email = userRequest.getEmail().toLowerCase().trim();
		String password = userRequest.getPassword();

		boolean emailExists = emailExists(email);

		if (emailExists) {
			log.info("Email {} is already taken", email);
			throw new EmailNotAvailableException(email);
		}

		try {
			UserEntity newCustomer = new UserEntity();
			newCustomer.setEmail(email);
			newCustomer.setPasswordHash(passwordEncoder.encode(password));
			newCustomer.setRole(Role.CUSTOMER);
			newCustomer.setCreatedAt(LocalDateTime.now());
			userRepository.saveAndFlush(newCustomer);

			return modelMapper.map(userRequest, UserResponse.class);

		} catch (DataIntegrityViolationException ex) {
			log.info("Email {} is already taken", email);
			throw new EmailNotAvailableException(email);
		}
	}

	private boolean emailExists(String email) {
		String trimmedEmail = email.toLowerCase().trim();
		return userRepository.emailExists(trimmedEmail);
	}

}
