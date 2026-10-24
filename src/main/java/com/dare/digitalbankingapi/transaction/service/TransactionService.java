package com.dare.digitalbankingapi.transaction.service;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountService accountService;
	private final SortValidator sortValidator;

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
			"id",
			"amount",
			"type",
			"createdAt",
			"balanceAfter",
			"transfer"
	);

	@Transactional
	public TransactionResponse deposit(TransactionRequest request) {

		AccountEntity account = accountService.getAccount(request.getAccountId());
		BigDecimal amount = request.getAmount();
		accountService.checkAccountStatus(account);
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
		log.info("Deposit transaction with id {} has been created ", transaction.getId());

		return buildResponse(transaction);
	}

	@Transactional
	public TransactionResponse withdraw(AuthenticatedUser authenticatedUser, TransactionRequest request) {

		AccountEntity account = accountService.getAccount(request.getAccountId());

		boolean userIsOwner = accountService.userIsOwner(account, authenticatedUser);

		if (!userIsOwner) {
			log.info("User id {} tried to withdraw from another user's account ", authenticatedUser.getUserId());
			throw new AccountNotFoundException(request.getAccountId());
		}

		accountService.checkAccountStatus(account);

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
		log.info("Withdraw transaction with id {} has been created ", transaction.getId());

		return buildResponse(transaction);
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

	public Page<TransactionResponse> viewAllTransactions(Pageable pageable) {
		sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

		log.debug("Getting page of transactions");

		return transactionRepository.findAll(pageable)
				.map(this::buildResponse);
	}

	public List<TransactionResponse> getAllTransactionsByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		Long userId = authenticatedUser.getUserId();

		List<TransactionEntity> transactions = transactionRepository.findAllByAccount_Owner_Id(userId);

		return transactions.stream().map(this::buildTransactionResponseWithDetails)
				.toList();
	}

	public TransactionResponse findTransactionDetailsById(AuthenticatedUser authenticatedUser, Long transactionId) {
		TransactionEntity transaction = getTransactionById(transactionId, authenticatedUser);
		return buildTransactionResponseWithDetails(transaction);

	}

	//Helper

	private TransactionResponse buildResponse(TransactionEntity transaction) {
		return TransactionResponse.builder()
				.fromAccountId(transaction.getAccount().getId())
				.amount(transaction.getAmount())
				.balanceAfter(transaction.getBalanceAfter())
				.build();
	}

	private TransactionResponse buildTransactionResponseWithDetails(TransactionEntity transaction) {
		TransferEntity transfer = transaction.getTransfer();

		return TransactionResponse.builder()
				.fromAccountId(
						transfer != null
								? transfer.getFromAccount().getId()
								: transaction.getAccount().getId()
				)
				.toAccountId(
						transfer != null
								? transfer.getToAccount().getId()
								: null
				)
				.amount(transaction.getAmount())
				.reference(transaction.getReference())
				.balanceAfter(transaction.getBalanceAfter())
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

	public TransactionEntity getTransactionById(Long transactionId, AuthenticatedUser authenticatedUser) {
		return transactionRepository
				.findByIdAndAccount_Owner_Id(transactionId, authenticatedUser.getUserId())
				.orElseThrow(() -> {
					log.info("Transaction with id {} has been found", transactionId);
					return new TransactionNotFoundException(transactionId);
				});
	}
}