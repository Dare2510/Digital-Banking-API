package com.dare.digitalbankingapi.user.dto;

import com.dare.digitalbankingapi.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailsResponse {

	private Long id;
	private String email;
	private UserRole userRole;
	private String name;
	private String surname;
	private String street;
	private String houseNumber;
	private String city;
	private String zipCode;
	private String country;

}
