package com.dare.digitalbankingapi.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileResponse {

	private String name;
	private String surname;
	private String street;
	private String houseNumber;
	private String city;
	private String zipCode;
	private String country;
}
