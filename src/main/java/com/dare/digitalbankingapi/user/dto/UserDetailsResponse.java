package com.dare.digitalbankingapi.user.dto;

import com.dare.digitalbankingapi.user.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
