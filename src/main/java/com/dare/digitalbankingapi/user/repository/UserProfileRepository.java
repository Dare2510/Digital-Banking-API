package com.dare.digitalbankingapi.user.repository;

import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

	boolean existsByUserId(Long userId);

	Optional<UserProfileEntity> findByUserId(Long userId);
}
