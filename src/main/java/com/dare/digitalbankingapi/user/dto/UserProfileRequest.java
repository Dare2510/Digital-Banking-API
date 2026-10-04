package com.dare.digitalbankingapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileRequest {

	@NotBlank(message = "Name is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "Name can only contain letters"
	)
	@Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
	private String name;

	@NotBlank(message = "Surname is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "Surname can only contain letters"
	)
	@Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
	private String surname;

	@NotBlank(message = "Street is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "Street can only contain letters"
	)
	@Size(min = 2, max = 20, message = "Name must be between 2 and 100 characters")
	private String street;

	@NotBlank(message = "House number is required")
	@Pattern(
			regexp = "^[0-9]+[A-Za-z]?(?:[-/][0-9A-Za-z]+)?$",
			message = "Invalid house number"
	)
	@Size(min = 1, max = 10, message = "House number must be between 1 and 10 characters")
	private String houseNumber;

	@NotBlank(message = "City is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "City can only contain letters"
	)
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String city;

	@NotBlank(message = "Zip-Code is required")
	@Pattern(
			regexp = "^[A-Za-z0-9 -]{3,10}$",
			message = "Invalid zip code"
	)
	@Size(min = 2, max = 15, message = "Name must be between 2 and 15 characters")
	private String zipCode;

	@NotBlank(message = "Country is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "Country can only contain letters"
	)
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String country;
}
