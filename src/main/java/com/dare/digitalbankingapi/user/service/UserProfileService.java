package com.dare.digitalbankingapi.user.service;

import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.exceptions.UserProfileAlreadyExistsException;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserProfileService {

	private final UserService userService;
	private final UserProfileRepository userProfileRepository;
	private final ModelMapper modelMapper;

	@Transactional
	public UserProfileResponse createUserProfile(UserProfileRequest userProfileRequest) {

		UserEntity connectedUser = userService.getUserById(userProfileRequest.getUserId());

		boolean profileExists = userProfileRepository.existsByUserId(userProfileRequest.getUserId());

		if (profileExists) {
			log.info("User Profile for user {} already exists", connectedUser.getId());
			throw new UserProfileAlreadyExistsException(connectedUser.getId());
		}

		UserProfileEntity profileEntity = new UserProfileEntity(
				connectedUser,
				userProfileRequest.getName(),
				userProfileRequest.getSurname(),
				userProfileRequest.getStreet(),
				userProfileRequest.getHouseNumber(),
				userProfileRequest.getCity(),
				userProfileRequest.getZipCode(),
				userProfileRequest.getCountry()
		);
		try {
			UserProfileEntity savedProfile = userProfileRepository.saveAndFlush(profileEntity);

			return modelMapper.map(savedProfile, UserProfileResponse.class);

		} catch (DataIntegrityViolationException e) {

			log.warn("Could not create profile for user {}", connectedUser.getId());

			throw new UserProfileAlreadyExistsException(connectedUser.getId());
		}
	}


}
