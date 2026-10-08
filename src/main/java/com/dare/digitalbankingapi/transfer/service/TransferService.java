package com.dare.digitalbankingapi.transfer.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.exceptions.NotEqualCurrencyException;
import com.dare.digitalbankingapi.transfer.exceptions.SufficientBalanceException;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Log4j2
@RequiredArgsConstructor
public class TransferService {

	private final TransferRepository transferRepository;
	private final AccountService accountService;

	public TransferResponse createTransferBetweenAccounts(TransferRequest transferRequest) {

		Long fromAccountId = transferRequest.getFromAccountId();
		Long toAccountId = transferRequest.getToAccountId();


		AccountEntity fromAccount = accountService.getAccount(fromAccountId);
		AccountEntity toAccount = accountService.getAccount(toAccountId);

		BigDecimal currentBalance = fromAccount.getBalance();
		BigDecimal transferAmount = transferRequest.getAmount();

		checkBalance(fromAccount,toAccount,transferAmount,currentBalance);
		checkCurrencies(fromAccount,toAccount);

		TransferEntity transfer = new TransferEntity(
				fromAccount,
				toAccount,
				transferAmount,
				fromAccount.getCurrency()
		);






	}

	//Helper Methods
	//Validators

	private void checkBalance(AccountEntity fromAccount, AccountEntity toAccount, BigDecimal transferAmount, BigDecimal currentBalance) {
		boolean enoughBalance = currentBalance.subtract(transferAmount).compareTo(BigDecimal.ZERO) >= 0;

		if(!enoughBalance) {
			log.info("Balance is insufficient");
			throw new SufficientBalanceException();
		}

	}

	private void checkCurrencies(AccountEntity fromAccount, AccountEntity toAccount) {
		Currency fromCurrency = fromAccount.getCurrency();
		Currency toCurrency = toAccount.getCurrency();

		boolean currencyIsEqual = fromCurrency.equals(toCurrency);

		if(!currencyIsEqual) {
			log.info("Currency is not equal");
			throw new NotEqualCurrencyException();
		}
	}


}
