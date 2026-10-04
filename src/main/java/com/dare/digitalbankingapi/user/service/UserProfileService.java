package com.dare.digitalbankingapi.user.service;

import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final UserService userService;
	private final UserProfileRepository userProfileRepository;
	private final ModelMapper modelMapper;

	@Transactional
	public UserProfileResponse createUserProfile(UserProfileRequest userProfileRequest) {
		UserEntity connectedUser = userService.getUserById(userProfileRequest.getUserId());

		UserProfileEntity profileEntity = new UserProfileEntity();
		profileEntity.setUser(connectedUser);
		profileEntity.setName(userProfileRequest.getName());
		profileEntity.setSurname(userProfileRequest.getSurname());
		profileEntity.setStreet(userProfileRequest.getStreet());
		profileEntity.setHouseNumber(userProfileRequest.getHouseNumber());
		profileEntity.setCity(userProfileRequest.getCity());
		profileEntity.setZipCode(userProfileRequest.getZipCode());
		profileEntity.setCountry(userProfileRequest.getCountry());
		profileEntity.setCreatedAt(LocalDateTime.now());
		profileEntity.setUpdatedAt(LocalDateTime.now());

		userProfileRepository.save(profileEntity);

		return modelMapper.map(profileEntity, UserProfileResponse.class);

	}


}
