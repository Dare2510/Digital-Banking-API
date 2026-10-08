package com.dare.digitalbankingapi.transfer.repository;

import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

}
