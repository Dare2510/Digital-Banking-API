package com.dare.digitalbankingapi.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@NoArgsConstructor
@Setter
@Getter
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
}
