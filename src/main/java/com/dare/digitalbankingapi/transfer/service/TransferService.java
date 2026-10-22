package com.dare.digitalbankingapi.transfer.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.entity.TransactionEntity;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transaction.exceptions.SameAccountTransferException;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import com.dare.digitalbankingapi.transfer.entity.TransferStatus;
import com.dare.digitalbankingapi.transfer.exceptions.InsufficientBalanceException;
import com.dare.digitalbankingapi.transfer.exceptions.NotEqualCurrencyException;
import com.dare.digitalbankingapi.transfer.exceptions.TransferNotFoundException;
import com.dare.digitalbankingapi.transfer.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
@RequiredArgsConstructor
public class TransferService {

	private final TransferRepository transferRepository;
	private final AccountService accountService;
	private final TransactionService transactionService;
	private final SortValidator sortValidator;

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
			"fromAccount",
			"toAccount",
			"currency",
			"status",
			"createdAt"
	);

	@Transactional
	public TransferResponse createTransferBetweenAccounts(AuthenticatedUser authenticatedUser,TransferRequest transferRequest) {

		AccountEntity fromAccount = accountService.getAccount(transferRequest.getFromAccountId());

		boolean userIsOwner = accountService.userIsOwner(fromAccount,authenticatedUser);

		if(!userIsOwner) {
			log.info("User id {} tried to transfer from another user's account " , authenticatedUser.getUserId());
			throw new AccountNotFoundException(transferRequest.getFromAccountId());
		}


		AccountEntity toAccount = accountService.getAccount(transferRequest.getToAccountId());

		if (fromAccount.getId().equals(toAccount.getId())) {
			throw new SameAccountTransferException();
		}

		accountService.checkAccountStatus(fromAccount);
		accountService.checkAccountStatus(toAccount);

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
		log.info("Transfer from account id {} to account id {} has been created", fromAccount, toAccount);

		accountService.updateBalance(fromAccount, transferAmount, TransactionType.TRANSFER_OUT);

		accountService.updateBalance(toAccount, transferAmount, TransactionType.TRANSFER_IN);

		TransactionEntity fromAccountTransaction = transactionService.createTransferTransaction(
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

		return buildTransferResponse(transfer, fromAccountTransaction);
	}

	public TransferResponse viewTransferDetails(AuthenticatedUser authenticatedUser,Long transferId) {
		TransferEntity transfer = getTransferEntityById(transferId);

		boolean userIsOwnerOfTransfer = accountService.userIsOwner(transfer.getFromAccount(),authenticatedUser);

		if(!userIsOwnerOfTransfer) {
			log.info("User id {} tried to view another user's transfer " , authenticatedUser.getUserId());
			throw new TransferNotFoundException(transferId);
		}

		TransactionEntity transaction = transactionService.getTransactionByTransferId(transferId);

		log.info("Getting transfer details for transfer id {}", transferId);

		return buildTransferResponse(transfer, transaction);
	}

	public List<TransferResponse> findByAccountId(AuthenticatedUser authenticatedUser,Long accountId) {
		AccountEntity account = accountService.getAccount(accountId);

		boolean userIsOwner = accountService.userIsOwner(account,authenticatedUser);

		if(!userIsOwner) {
			log.info("User id {} tried to view another user's account " , authenticatedUser.getUserId());
			throw new AccountNotFoundException(accountId);
		}

		List<TransferEntity> transfers = transferRepository.
				findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(
						accountId,
						accountId
				);

		if (transfers.isEmpty()) {
			throw new TransferNotFoundException();
		}

		return transfers.stream()
				.map(transfer ->
						buildTransferResponse(transfer,
								transactionService.getTransactionByTransferId(transfer.getId())))
				.toList();
	}

	public Page<TransferResponse> viewAllTransfers(Pageable pageable) {
		sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

		log.debug("Getting Page of transfer details");

		return transferRepository.findAll(pageable)
				.map(transfer -> buildTransferResponse(
						transfer, transactionService.getTransactionByTransferId(transfer.getId())));

	}

	//Helper Methods

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

	private TransferResponse buildTransferResponse(TransferEntity transfer, TransactionEntity fromAccount) {

		return TransferResponse.builder()
				.fromAccountId(transfer.getFromAccount().getId())
				.toAccountId(transfer.getToAccount().getId())
				.transferId(transfer.getId())
				.amount(transfer.getAmount())
				.reference(fromAccount.getReference())
				.build();
	}

	private TransferEntity getTransferEntityById(Long transferId) {

		return transferRepository.findById(transferId).orElseThrow(
				() -> {
					log.info("Transfer with id {} not found", transferId);
					return new TransferNotFoundException(transferId);
				}
		);
	}
}