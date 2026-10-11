package com.dare.digitalbankingapi.transaction.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.exceptions.TransactionNotFoundException;
import com.dare.digitalbankingapi.transaction.repository.TransactionRepository;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountService accountService;

	@Transactional
	public TransactionResponse deposit(TransactionRequest request) {

		AccountEntity account = accountService.getAccount(request.getAccountId());

		BigDecimal amount = request.getAmount();

		accountService.updateBalance(account, amount, TransactionType.DEPOSIT);

		TransactionEntity transaction =
				new TransactionEntity(
						account,
						amount,
						TransactionType.DEPOSIT.name(),
						TransactionType.DEPOSIT,
						account.getBalance()
				);

		transactionRepository.saveAndFlush(transaction);
		log.info("Deposit transaction with id {} has been created ",transaction.getId());

		return buildResponse(account, amount);
	}

	@Transactional
	public TransactionResponse withdraw(TransactionRequest request) {

		AccountEntity account = accountService.getAccount(request.getAccountId());

		BigDecimal amount = request.getAmount();

		BigDecimal balanceAfter = account.getBalance().subtract(amount);

		if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientBalanceException(
					TransactionType.WITHDRAW
			);
		}

		accountService.updateBalance(account, amount, TransactionType.WITHDRAW);

		TransactionEntity transaction =
				new TransactionEntity(
						account,
						amount,
						TransactionType.WITHDRAW.name(),
						TransactionType.WITHDRAW,
						account.getBalance()
				);

		transactionRepository.saveAndFlush(transaction);
		log.info("Withdraw transaction with id {} has been created ",transaction.getId());

		return buildResponse(account, amount);
	}

	public TransactionEntity createTransferTransaction(AccountEntity account, String reference,
										  TransferEntity transfer, TransactionType transactionType) {

		TransactionEntity transaction =
				new TransactionEntity(
						account,
						transfer.getAmount(),
						reference,
						transactionType,
						account.getBalance(),
						transfer
				);

		transactionRepository.saveAndFlush(transaction);
		log.info("Transfer Transaction with {} id has been created", transaction.getId());

		return transaction;
	}

	private TransactionResponse buildResponse(AccountEntity account, BigDecimal amount) {

		return TransactionResponse.builder()
				.fromAccountId(account.getId())
				.amount(amount)
				.balanceAfter(account.getBalance())
				.build();
	}

	public TransactionEntity getTransactionByTransferId(Long transferId) {
		return transactionRepository.findFirstByTransferId(transferId)
				.orElseThrow(
						() -> {
							log.info("Transaction for transfer id {} not found", transferId);
							return new TransactionNotFoundException(transferId);
						}
				);
	}
}