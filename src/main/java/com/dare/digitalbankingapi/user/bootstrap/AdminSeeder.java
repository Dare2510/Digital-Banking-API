package com.dare.digitalbankingapi.user.bootstrap;

import com.dare.digitalbankingapi.user.entity.ProfileStatus;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.repository.UserProfileRepository;
import com.dare.digitalbankingapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${ADMIN_EMAIL}")
	private String adminEmail;

	@Value("${ADMIN_PASSWORD}")
	private String adminPassword;


	@Override
	public void run(String... args) throws Exception {
		boolean adminExists = userRepository.existsByUserRole(UserRole.ADMIN);

		if (adminExists) {
			return;
		}

		UserEntity admin = new UserEntity();
		admin.setEmail(adminEmail);
		admin.setUserRole(UserRole.ADMIN);
		admin.setCreatedAt(LocalDateTime.now());
		admin.setPasswordHash(passwordEncoder.encode(adminPassword));


		userRepository.saveAndFlush(admin);

		UserProfileEntity adminProfile = new UserProfileEntity();
		adminProfile.setUser(admin);
		adminProfile.setName("System");
		adminProfile.setSurname("System_generated");
		adminProfile.setStreet("System Street");
		adminProfile.setCity("System City");
		adminProfile.setHouseNumber("99");
		adminProfile.setZipCode("System Zip Code");
		adminProfile.setCountry("System Country");
		adminProfile.setCreatedAt(LocalDateTime.now());
		adminProfile.setStatus(ProfileStatus.COMPLETE);

		userProfileRepository.saveAndFlush(adminProfile);

		log.info("Boot up admin has been created");


	}
}
