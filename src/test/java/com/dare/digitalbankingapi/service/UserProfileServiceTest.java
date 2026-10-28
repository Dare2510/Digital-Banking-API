package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.dto.UserProfileRequest;
import com.dare.digitalbankingapi.user.dto.UserProfileResponse;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.exceptions.UserProfileAlreadyExistsException;
import com.dare.digitalbankingapi.user.exceptions.UserProfileUpdateFailedNoProfileExists;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

	private UserProfileService userProfileService;

	@Mock
	private UserService userService;

	@Mock
	private UserProfileRepository userProfileRepository;
	@Spy
	private ModelMapper modelMapper;

	private final String NAME = "testName";
	private final String SURNAME = "testSurName";
	private final String STREET = "testStreet";
	private final String HOUSE_NUMBER = "99";
	private final String CITY = "testCity";
	private final String ZIPCODE = "testZipCode";
	private final String COUNTRY = "testCountry";

	private final String UPDATED_CITY = "testUpdatedCity";
	private final String UPDATED_STREET = "testUpdatedStreet";
	private final String UPDATED_HOUSE_NUMBER = "testUpdatedHouseNumber";
	private final String UPDATED_ZIPCODE = "testUpdatedZipCode";


	//UserEntity Fields
	private final String EMAIL = "testEmail";
	private final Long USER_ID = 1L;
	private final UserRole USER_ROLE = UserRole.CUSTOMER;


	@BeforeEach
	public void setUp() {
		userProfileService = new UserProfileService(userService
				, userProfileRepository, modelMapper);
	}

	//Helper
	private UserProfileRequest newUserProfileRequest() {
		return new UserProfileRequest(NAME, SURNAME, STREET,
				HOUSE_NUMBER, CITY, ZIPCODE, COUNTRY);
	}

	private UserProfileRequest updateduserProfileRequest() {
		return new UserProfileRequest(NAME, SURNAME, UPDATED_STREET,
				UPDATED_HOUSE_NUMBER, UPDATED_CITY, UPDATED_ZIPCODE, COUNTRY);
	}

	private AuthenticatedUser returnAuthenticatedUser() {
		return new AuthenticatedUser(
				USER_ID, EMAIL, USER_ROLE
		);

	}

	private UserEntity returnUserEntity() {
		String HASHED_PASSWORD = "hashedPassword";

		UserEntity user = new UserEntity(
				EMAIL, HASHED_PASSWORD, USER_ROLE
		);

		user.setId(USER_ID);

		return user;
	}

	private void profileSetter(UserProfileEntity profile, UserProfileRequest userProfileRequest) {

		profile.setName(userProfileRequest.getName());
		profile.setSurname(userProfileRequest.getSurname());
		profile.setStreet(userProfileRequest.getStreet());
		profile.setHouseNumber(userProfileRequest.getHouseNumber());
		profile.setCity(userProfileRequest.getCity());
		profile.setZipCode(userProfileRequest.getZipCode());
		profile.setCountry(userProfileRequest.getCountry());


	}


	@Test
	public void testCreateUserProfile_whenRequestIsValid_returnUserProfileResponse() {

		AuthenticatedUser loggedInUser = returnAuthenticatedUser();
		UserProfileRequest validProfileRequest = newUserProfileRequest();
		UserEntity user = returnUserEntity();

		when(userService.getUserById(loggedInUser.getUserId())).thenReturn(user);
		when(userProfileRepository.existsByUserId(loggedInUser.getUserId())).thenReturn(false);
		when(userProfileRepository.saveAndFlush(any(UserProfileEntity.class))).thenAnswer(
				invocation -> {

					UserProfileEntity profile = invocation.getArgument(0);
					profileSetter(profile, validProfileRequest);
					return profile;
				});


		UserProfileResponse userProfileResponse = userProfileService.createUserProfile(loggedInUser, validProfileRequest);

		assertEquals(NAME, userProfileResponse.getName());
		assertEquals(SURNAME, userProfileResponse.getSurname());
		assertEquals(STREET, userProfileResponse.getStreet());
		assertEquals(HOUSE_NUMBER, userProfileResponse.getHouseNumber());
		assertEquals(CITY, userProfileResponse.getCity());
		assertEquals(ZIPCODE, userProfileResponse.getZipCode());
		assertEquals(COUNTRY, userProfileResponse.getCountry());

		verify(userProfileRepository, times(1)).existsByUserId(loggedInUser.getUserId());
		verify(userService, times(1)).getUserById(loggedInUser.getUserId());
		verify(userProfileRepository, times(1)).saveAndFlush(any(UserProfileEntity.class));


	}

	@Test
	public void createUserProfile_whenUserProfileExists_throwsUserProfileServiceException() {
		AuthenticatedUser loggedInUser = returnAuthenticatedUser();
		UserProfileRequest existingRequest = newUserProfileRequest();
		UserEntity user = returnUserEntity();

		when(userService.getUserById(loggedInUser.getUserId())).thenReturn(user);
		when(userProfileRepository.existsByUserId(loggedInUser.getUserId())).thenReturn(true);

		assertThatThrownBy(() ->
				userProfileService.createUserProfile(loggedInUser.getUserId(), existingRequest))
				.isInstanceOf(UserProfileAlreadyExistsException.class)
				.hasMessage("UserProfile already exists for user id: " + loggedInUser.getUserId());

		verify(userService, times(1)).getUserById(loggedInUser.getUserId());
		verify(userProfileRepository, never()).saveAndFlush(any(UserProfileEntity.class));
	}

	@Test
	public void updateUserProfile_whenUserProfileExists_returnUserProfileResponse() {
		AuthenticatedUser loggedInUser = returnAuthenticatedUser();
		UserProfileRequest updateRequest = updateduserProfileRequest();
		UserEntity user = returnUserEntity();
		UserProfileEntity profile = new UserProfileEntity();
		profileSetter(profile, updateRequest);

		when(userService.getUserById(loggedInUser.getUserId())).thenReturn(user);
		when(userProfileRepository.existsByUserId(loggedInUser.getUserId())).thenReturn(true);

		when(userProfileRepository.findByUserId(loggedInUser.getUserId())).thenReturn(Optional.of(profile));

		UserProfileResponse userProfileResponse = userProfileService.updateUserProfile(loggedInUser, updateRequest);

		assertEquals(UPDATED_CITY, userProfileResponse.getCity());
		assertEquals(UPDATED_HOUSE_NUMBER, userProfileResponse.getHouseNumber());
		assertEquals(UPDATED_STREET, userProfileResponse.getStreet());
		assertEquals(UPDATED_ZIPCODE, userProfileResponse.getZipCode());

		verify(userService, times(1)).getUserById(loggedInUser.getUserId());
		verify(userProfileRepository, times(1)).findByUserId(loggedInUser.getUserId());
		verify(userProfileRepository, times(1)).existsByUserId(loggedInUser.getUserId());
		verify(userProfileRepository, times(1)).saveAndFlush(any(UserProfileEntity.class));


	}

	@Test
	public void updateUserProfile_whenUserProfileDoesNotExists_throwsUserProfileUpdateFailedNoProfileExistsException() {
		AuthenticatedUser loggedInUser = returnAuthenticatedUser();
		UserProfileRequest updateRequestOfNotExistingProfile = newUserProfileRequest();
		UserEntity user = returnUserEntity();

		when(userService.getUserById(loggedInUser.getUserId())).thenReturn(user);
		when(userProfileRepository.existsByUserId(loggedInUser.getUserId())).thenReturn(false);

		assertThatThrownBy(() ->
				userProfileService.updateUserProfile(loggedInUser, updateRequestOfNotExistingProfile))
				.isInstanceOf(UserProfileUpdateFailedNoProfileExists.class)
				.hasMessage("Profile update failed for user id: " + USER_ID + "no profile exists");

		verify(userService, times(1)).getUserById(loggedInUser.getUserId());
		verify(userProfileRepository, times(1)).existsByUserId(loggedInUser.getUserId());
		verify(userProfileRepository, never()).saveAndFlush(any(UserProfileEntity.class));

	}


}
