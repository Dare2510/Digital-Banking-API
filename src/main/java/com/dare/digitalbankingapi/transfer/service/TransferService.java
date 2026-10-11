package com.dare.digitalbankingapi.transfer.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.entity.TransferStatus;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import com.dare.digitalbankingapi.transfer.exceptions.NotEqualCurrencyException;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class TransferService {

	private final TransferRepository transferRepository;
	private final AccountService accountService;
	private final TransactionService transactionService;

	@Transactional
	public TransferResponse createTransferBetweenAccounts(
			TransferRequest transferRequest) {

		AccountEntity fromAccount = accountService.getAccount(transferRequest.getFromAccountId());

		AccountEntity toAccount = accountService.getAccount(transferRequest.getToAccountId());

		BigDecimal transferAmount = transferRequest.getAmount();

		checkBalance(transferAmount, fromAccount.getBalance());

		checkCurrencies(fromAccount, toAccount);

		TransferEntity transfer =
				new TransferEntity(
						fromAccount,
						toAccount,
						transferAmount,
						fromAccount.getCurrency()
				);

		transferRepository.saveAndFlush(transfer);

		accountService.updateBalance(fromAccount, transferAmount, TransactionType.TRANSFER_OUT);

		accountService.updateBalance(toAccount, transferAmount, TransactionType.TRANSFER_IN);

		transactionService.createTransferTransaction(
				fromAccount,
				transferRequest.getReference(),
				transfer,
				TransactionType.TRANSFER_OUT
		);

		transactionService.createTransferTransaction(
				toAccount,
				transferRequest.getReference(),
				transfer,
				TransactionType.TRANSFER_IN
		);

		transfer.setStatus(TransferStatus.COMPLETED);
		transfer.setUpdatedAt(LocalDateTime.now());

		return buildTransferResponse(transfer, transferRequest);
	}

	private void checkBalance(BigDecimal transferAmount, BigDecimal currentBalance) {

		boolean enoughBalance = currentBalance
						.subtract(transferAmount)
						.compareTo(BigDecimal.ZERO) >= 0;

		if (!enoughBalance) {
			log.info("Balance is insufficient");
			throw new InsufficientBalanceException();
		}
	}

	private void checkCurrencies(AccountEntity fromAccount, AccountEntity toAccount) {

		Currency fromCurrency = fromAccount.getCurrency();
		Currency toCurrency = toAccount.getCurrency();

		if (!fromCurrency.equals(toCurrency)) {
			log.info("Currencies are not equal");
			throw new NotEqualCurrencyException();
		}
	}

	private TransferResponse buildTransferResponse(TransferEntity transfer, TransferRequest transferRequest) {

		return TransferResponse.builder()
				.fromAccountId(transferRequest.getFromAccountId())
				.toAccountId(transferRequest.getToAccountId())
				.transferId(transfer.getId())
				.amount(transfer.getAmount())
				.reference(transferRequest.getReference())
				.build();
	}
}