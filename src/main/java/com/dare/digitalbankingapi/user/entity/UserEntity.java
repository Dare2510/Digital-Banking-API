package com.dare.digitalbankingapi.user.entity;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
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

	private String name;
	private String surname;
	private String email;
	private String street;
	private String city;
	private String zipCode;
	private String country;

	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private Role role;

	private LocalDateTime createdAt;
}
