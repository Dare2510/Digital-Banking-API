package com.dare.digitalbankingapi.transaction.service;

import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final TransferRepository transferRepository;

	public TransactionEntity createTransaction(TransferEntity transferEntity, TransactionType transactionType) {

	}
}
