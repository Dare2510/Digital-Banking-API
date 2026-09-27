package com.dare.digitalbankingapi.account.repository;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

}
