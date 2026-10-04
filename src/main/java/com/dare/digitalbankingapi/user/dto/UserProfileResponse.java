package com.dare.digitalbankingapi.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserProfileResponse {

	private final String name;
	private final String surname;
	private final String street;
	private final String houseNumber;
	private final String city;
	private final String zipCode;
	private final String country;
}
