package com.dare.digitalbankingapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

	@Schema(example = "Arnold")
	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 50)
	@Pattern(
			regexp = "^[\\p{L}\\p{M}]+(?:[ '\\-’][\\p{L}\\p{M}]+)*$",
			message = "Name contains invalid characters"
	)
	private String name;

	@Schema(example = "Alpha")
	@NotBlank(message = "Surname is required")
	@Size(min = 2, max = 50)
	@Pattern(
			regexp = "^[\\p{L}\\p{M}]+(?:[ '\\-’][\\p{L}\\p{M}]+)*$",
			message = "Surname contains invalid characters"
	)
	private String surname;

	@Schema(example = "Example street")
	@NotBlank(message = "Street is required")
	@Size(min = 2, max = 100)
	@Pattern(
			regexp = "^[\\p{L}\\p{M}0-9 .,'’/\\-]+$",
			message = "Street contains invalid characters"
	)
	private String street;

	@Schema(example = "55")
	@NotBlank(message = "House number is required")
	@Size(min = 1, max = 10)
	@Pattern(
			regexp = "^[0-9]+[A-Za-z]?(?:[-/][0-9A-Za-z]+)?$",
			message = "Invalid house number"
	)
	private String houseNumber;

	@Schema(example = "Example city")
	@NotBlank(message = "City is required")
	@Size(min = 2, max = 100)
	@Pattern(
			regexp = "^[\\p{L}\\p{M} .,'’()\\-]+$",
			message = "City contains invalid characters"
	)
	private String city;

	@Schema(example = "1234567")
	@NotBlank(message = "Zip code is required")
	@Size(min = 3, max = 15)
	@Pattern(
			regexp = "^[A-Za-z0-9][A-Za-z0-9 \\-]{1,13}[A-Za-z0-9]$",
			message = "Invalid zip code"
	)
	private String zipCode;

	@Schema(example = "Example country")
	@NotBlank(message = "Country is required")
	@Size(min = 2, max = 100)
	@Pattern(
			regexp = "^[\\p{L}\\p{M} .,'’()\\-]+$",
			message = "Country contains invalid characters"
	)
	private String country;
}
