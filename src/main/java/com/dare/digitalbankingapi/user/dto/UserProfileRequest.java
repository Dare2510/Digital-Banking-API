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

	// temporary until Security
	private Long userId;

	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 50)
	@Pattern(
			regexp = "^[\\p{L}\\p{M}]+(?:[ '\\-’][\\p{L}\\p{M}]+)*$",
			message = "Name contains invalid characters"
	)
	private String name;

	@NotBlank(message = "Surname is required")
	@Size(min = 2, max = 50)
	@Pattern(
			regexp = "^[\\p{L}\\p{M}]+(?:[ '\\-’][\\p{L}\\p{M}]+)*$",
			message = "Surname contains invalid characters"
	)
	private String surname;

	@NotBlank(message = "Street is required")
	@Size(min = 2, max = 100)
	@Pattern(
			regexp = "^[\\p{L}\\p{M}0-9 .,'’/\\-]+$",
			message = "Street contains invalid characters"
	)
	private String street;

	@NotBlank(message = "House number is required")
	@Size(min = 1, max = 10)
	@Pattern(
			regexp = "^[0-9]+[A-Za-z]?(?:[-/][0-9A-Za-z]+)?$",
			message = "Invalid house number"
	)
	private String houseNumber;

	@NotBlank(message = "City is required")
	@Size(min = 2, max = 100)
	@Pattern(
			regexp = "^[\\p{L}\\p{M} .,'’()\\-]+$",
			message = "City contains invalid characters"
	)
	private String city;

	@NotBlank(message = "Zip code is required")
	@Size(min = 3, max = 15)
	@Pattern(
			regexp = "^[A-Za-z0-9][A-Za-z0-9 \\-]{1,13}[A-Za-z0-9]$",
			message = "Invalid zip code"
	)
	private String zipCode;

	@NotBlank(message = "Country is required")
	@Size(min = 2, max = 100)
	@Pattern(
			regexp = "^[\\p{L}\\p{M} .,'’()\\-]+$",
			message = "Country contains invalid characters"
	)
	private String country;
}
