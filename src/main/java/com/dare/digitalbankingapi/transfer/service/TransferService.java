package com.dare.digitalbankingapi.transfer.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.dto.DepositAndWithdrawalRequest;
import com.dare.digitalbankingapi.transfer.dto.DepositAndWithdrawalResponse;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import com.dare.digitalbankingapi.transfer.exceptions.NotEqualCurrencyException;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Log4j2
@RequiredArgsConstructor
public class TransferService {

	private final TransferRepository transferRepository;
	private final AccountService accountService;
	private final TransactionService transactionService;

	@Transactional
	public TransferResponse createTransferBetweenAccounts(TransferRequest transferRequest) {

		Long fromAccountId = transferRequest.getFromAccountId();
		Long toAccountId = transferRequest.getToAccountId();


		AccountEntity fromAccount = accountService.getAccount(fromAccountId);
		AccountEntity toAccount = accountService.getAccount(toAccountId);

		BigDecimal currentBalance = fromAccount.getBalance();
		BigDecimal transferAmount = transferRequest.getAmount();

		checkBalance(transferAmount, currentBalance);
		checkCurrencies(fromAccount, toAccount);

		TransferEntity transfer = new TransferEntity(
				fromAccount,
				toAccount,
				transferAmount,
				fromAccount.getCurrency()
		);

		transferRepository.saveAndFlush(transfer);

		transactionService.createTransaction(fromAccount, transferRequest.getReference(),
				transfer, TransactionType.TRANSFER_OUT);

		transactionService.createTransaction(fromAccount, transferRequest.getReference(),
				transfer, TransactionType.TRANSFER_IN);

		accountService.updateBalance(fromAccount, transferAmount, TransactionType.TRANSFER_OUT);
		accountService.updateBalance(toAccount, transferAmount, TransactionType.TRANSFER_IN);

		return buildTransferResponse(transfer, transferRequest);
	}

	@Transactional
	public DepositAndWithdrawalResponse depositAndWithdrawal(DepositAndWithdrawalRequest depositAndWithdrawalRequest, TransactionType transactionType) {

		transactionService.transactionTypeIsValidForDepositAndWithdrawal(transactionType);

		String reference = transactionType.name() + " " + LocalDate.now();

		AccountEntity accountToDeposit = accountService.getAccount(depositAndWithdrawalRequest.getAccountId());

		TransferEntity deposit = new TransferEntity(
				accountToDeposit,
				depositAndWithdrawalRequest.getAmount(),
				accountToDeposit.getCurrency()
		);

		transferRepository.saveAndFlush(deposit);


		transactionService.createTransaction(
				accountToDeposit,
				reference,
				deposit,
				transactionType);


		accountService.updateBalance(accountToDeposit, depositAndWithdrawalRequest.getAmount(), transactionType);

		return buildDepositAndWithdrawalResponse(accountToDeposit, depositAndWithdrawalRequest);

	}

	//Helper Methods
	//Validators

	private void checkBalance(BigDecimal transferAmount, BigDecimal currentBalance) {
		boolean enoughBalance = currentBalance.subtract(transferAmount).compareTo(BigDecimal.ZERO) >= 0;

		if (!enoughBalance) {
			log.info("Balance is insufficient");
			throw new InsufficientBalanceException();
		}

	}

	private void checkCurrencies(AccountEntity fromAccount, AccountEntity toAccount) {
		Currency fromCurrency = fromAccount.getCurrency();
		Currency toCurrency = toAccount.getCurrency();

		boolean currencyIsEqual = fromCurrency.equals(toCurrency);

		if (!currencyIsEqual) {
			log.info("Currency is not equal");
			throw new NotEqualCurrencyException();
		}
	}

	private DepositAndWithdrawalResponse buildDepositAndWithdrawalResponse(AccountEntity account,
																		   DepositAndWithdrawalRequest depositAndWithdrawalRequest) {
		return DepositAndWithdrawalResponse.builder()
				.fromAccountId(account.getId())
				.amount(depositAndWithdrawalRequest.getAmount())
				.balanceAfter(account.getBalance())
				.build();

	}

	private TransferResponse buildTransferResponse(TransferEntity transfer,
												   TransferRequest transferRequest) {

		return TransferResponse.builder()
				.fromAccountId(transferRequest.getFromAccountId())
				.toAccountId(transferRequest.getToAccountId())
				.transferId(transfer.getId())
				.amount(transfer.getAmount())
				.reference(transferRequest.getReference())
				.build();

	}


}
