package com.dare.digitalbankingapi.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;

	@OneToOne(
			mappedBy = "user",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private UserProfileEntity profile;

	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private UserRole userRole;

	private LocalDateTime createdAt;

	public UserEntity(String email, String passwordHash, UserRole userRole) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.userRole = userRole;
		this.createdAt = LocalDateTime.now();
	}
}
