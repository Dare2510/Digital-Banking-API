package com.dare.digitalbankingapi.user.service;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.entity.ProfileStatus;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.exceptions.UserProfileAlreadyExistsException;
import com.dare.digitalbankingapi.user.exceptions.UserProfileNotFoundException;
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
	public UserProfileResponse createUserProfile(AuthenticatedUser authenticatedUser, UserProfileRequest userProfileRequest) {

		UserEntity connectedUser = userService.getUserById(authenticatedUser.getUserId());

		boolean profileExists = userProfileRepository.existsByUserId(authenticatedUser.getUserId());

		if (profileExists) {
			log.info("User Profile for user {} already exists", connectedUser.getId());
			throw new UserProfileAlreadyExistsException(connectedUser.getId());
		}

		UserProfileEntity profileEntity = createUserProfileEntity(connectedUser, userProfileRequest);

		try {
			profileEntity.setStatus(ProfileStatus.COMPLETE);

			UserProfileEntity savedProfile = userProfileRepository.saveAndFlush(profileEntity);
			log.info("Created User Profile for user {} ", connectedUser.getId());


			return modelMapper.map(savedProfile, UserProfileResponse.class);

		} catch (DataIntegrityViolationException e) {

			log.warn("Could not create profile for user {}", connectedUser.getId());

			throw new UserProfileAlreadyExistsException(connectedUser.getId());
		}
	}

	//Overloaded for admin users to create staff/admin profiles
	@Transactional
	public UserProfileResponse createUserProfile(Long userId, UserProfileRequest userProfileRequest) {

		UserEntity userForProfileCreation = userService.getUserById(userId);

		boolean profileExists = userProfileRepository.existsByUserId(userId);

		if (profileExists) {
			log.info("Employee User Profile for user {} already exists", userForProfileCreation.getId());
			throw new UserProfileAlreadyExistsException(userForProfileCreation.getId());
		}

		UserProfileEntity profileEntity = createUserProfileEntity(userForProfileCreation, userProfileRequest);

		try {
			profileEntity.setStatus(ProfileStatus.COMPLETE);

			UserProfileEntity savedProfile = userProfileRepository.saveAndFlush(profileEntity);
			log.info("Created Employee User Profile for user {} ", userForProfileCreation.getId());


			return modelMapper.map(savedProfile, UserProfileResponse.class);

		} catch (DataIntegrityViolationException e) {

			log.warn("Could not create Employee profile for user {}", userForProfileCreation.getId());

			throw new UserProfileAlreadyExistsException(userForProfileCreation.getId());
		}
	}

	public UserProfileEntity getProfileEntity(Long userId) {
		return userProfileRepository.findByUserId(userId).orElseThrow(
				() -> {
					log.error("Could not find profile for user {}", userId);
					return new UserProfileNotFoundException(userId);
				});
	}

	public UserProfileEntity getProfileEntityOrNull(Long userId) {
		return userProfileRepository.findByUserId(userId).orElse(null);
	}

	private UserProfileEntity createUserProfileEntity(UserEntity user, UserProfileRequest userProfileRequest) {
		return new UserProfileEntity(
				user,
				userProfileRequest.getName(),
				userProfileRequest.getSurname(),
				userProfileRequest.getStreet(),
				userProfileRequest.getHouseNumber(),
				userProfileRequest.getCity(),
				userProfileRequest.getZipCode(),
				userProfileRequest.getCountry()
		);

	}


}
