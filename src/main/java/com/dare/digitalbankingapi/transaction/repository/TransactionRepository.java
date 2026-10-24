package com.dare.digitalbankingapi.transaction.repository;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	Optional<TransactionEntity> findFirstByTransferId(Long transferId);
	List<TransactionEntity> findAllByAccount_Owner_Id(Long userId);
}
