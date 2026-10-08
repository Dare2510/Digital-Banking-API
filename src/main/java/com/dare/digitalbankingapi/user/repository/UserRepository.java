package com.dare.digitalbankingapi.user.repository;

import com.dare.digitalbankingapi.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	boolean existsByEmail(String email);
}
