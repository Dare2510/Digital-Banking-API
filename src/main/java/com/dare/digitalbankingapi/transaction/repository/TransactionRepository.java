package com.dare.digitalbankingapi.transaction.repository;

import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	Optional<TransactionEntity> findByTransferIdAndType(Long transferId, TransactionType type);

	List<TransactionEntity> findAllByAccount_Owner_Id(Long userId);

	Optional<TransactionEntity> findByIdAndAccount_Owner_Id(Long transactionId, Long userId);
}
