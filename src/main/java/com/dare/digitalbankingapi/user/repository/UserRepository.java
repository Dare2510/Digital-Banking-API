package com.dare.digitalbankingapi.user.repository;

import com.dare.digitalbankingapi.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <UserEntity, Long> {
}
