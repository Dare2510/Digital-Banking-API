package com.dare.digitalbankingapi.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class UserProfileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private UserEntity user;

	private String name;
	private String surname;
	private String street;
	private String houseNumber;
	private String city;
	private String zipCode;
	private String country;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public UserProfileEntity(UserEntity user, String name, String surname, String street,
							 String houseNumber, String city, String zipCode, String country) {
		this.user = user;
		this.name = name;
		this.surname = surname;
		this.street = street;
		this.houseNumber = houseNumber;
		this.city = city;
		this.zipCode = zipCode;
		this.country = country;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();

	}

}
