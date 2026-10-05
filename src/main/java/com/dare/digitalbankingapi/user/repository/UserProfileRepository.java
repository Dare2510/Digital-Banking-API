package com.dare.digitalbankingapi.user.repository;

import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

	boolean existsByUserId(Long userId);
}
