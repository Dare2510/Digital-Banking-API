package com.dare.digitalbankingapi.user.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@NoArgsConstructor
public class UserProfileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String surname;
	private String street;
	private String houseNumber;
	private String city;
	private String zipCode;
	private String country;

	private LocalDateTime  createdAt;
	private LocalDateTime updatedAt;
}
