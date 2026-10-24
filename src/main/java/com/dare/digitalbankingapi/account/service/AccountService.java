package com.dare.digitalbankingapi.account.service;

import com.dare.digitalbankingapi.account.dto.AccountClosureResponse;
import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.dto.AccountStatusRequest;
import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.exceptions.AccountClosureFailedException;
import com.dare.digitalbankingapi.account.exceptions.AccountInvalidStatusTransitionException;
import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.account.exceptions.AccountOperationNotAllowedException;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserProfileService userProfileService;
	private final UserService userService;
	private final SortValidator sortValidator;

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
			"id",
			"owner",
			"accountNumber",
			"balance",
			"currency",
			"status",
			"createdAt"
	);

	@Transactional
	public AccountResponse createAccount(AuthenticatedUser authenticatedUser, AccountRequest accountRequest) {

		Long userId = authenticatedUser.getUserId();

		UserEntity owner = userService.getUserById(userId);
		UserProfileEntity ownerProfile = userProfileService.getProfileEntity(userId);

		AccountEntity newAccount = new AccountEntity(
				owner,
				generateAccountNumber(),
				accountRequest.getCurrency()
		);

		accountRepository.saveAndFlush(newAccount);
		log.info("Created account with id {}", newAccount.getId());

		return responseBuilder(ownerProfile, newAccount);
	}

	public AccountResponse viewAccountDetails(AuthenticatedUser authenticatedUser, Long accountId) {

		AccountEntity account = getAccount(accountId);

		boolean userIsOwner = userIsOwner(account, authenticatedUser);

		if (!userIsOwner) {
			log.info("User id {} tried to view another user's account ", authenticatedUser.getUserId());
			throw new AccountNotFoundException(accountId);
		}

		return responseBuilder(account);
	}

	//Overloaded version for staff

	public AccountResponse viewAccountDetails(Long accountId) {

		AccountEntity account = getAccount(accountId);

		UserProfileEntity userProfile = userProfileService.getProfileEntity(account.getOwner().getId());

		return responseBuilder(userProfile, account);
	}

	public List<AccountResponse> viewAllAccountsByUser(AuthenticatedUser authenticatedUser) {

		UserEntity owner = userService.getUserById(authenticatedUser.getUserId());

		log.info("Viewing all accounts with user id {}", owner.getId());

		return accountRepository.findByOwner(owner)
				.stream()
				.map(this::responseBuilder)
				.toList();
	}

	public Page<AccountResponse> viewAllAccounts(Pageable pageable) {
		sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

		log.debug("Getting Page of all accounts");

		return accountRepository.findAll(pageable)
				.map(account -> {
					UserProfileEntity userProfile = userProfileService.getProfileEntity(account.getOwner().getId());
					return responseBuilder(userProfile, account);
				});
	}

	public AccountEntity getAccount(Long accountId) {

		return accountRepository.findById(accountId)
				.orElseThrow(() -> {
					log.info(
							"Account with id {} not found",
							accountId
					);
					return new AccountNotFoundException(accountId);
				});
	}


	public void changeAccountStatus(Long accountId, AccountStatusRequest newStatus) {
		AccountEntity account = getAccount(accountId);

		AccountStatus currentStatus = account.getStatus();

		validStatusTransition(currentStatus, newStatus);

		account.setStatus(newStatus.getStatus());
		accountRepository.saveAndFlush(account);

		log.info("Account with id {} status changed from {} to {}",
				accountId, currentStatus, newStatus);
	}


	public AccountClosureResponse closeAccount(AuthenticatedUser authenticatedUser, Long accountId) {
		AccountEntity account = getAccount(accountId);
		boolean balanceIsZero = balanceIsZero(account);
		boolean accountIsActive = accountIsActive(account);
		boolean userIsOwner = userIsOwner(account, authenticatedUser);

		if (!userIsOwner) {
			log.info("User id {} tried to close another user's account ", authenticatedUser.getUserId());
			throw new AccountNotFoundException(accountId);
		}

		if (balanceIsZero && accountIsActive) {
			log.info("Account with id {} has been closed", accountId);
			account.setStatus(AccountStatus.CLOSED);
			accountRepository.saveAndFlush(account);

			return new AccountClosureResponse(account.getId(), account.getAccountNumber());

		} else {
			log.info("Cannot close Account with id {} ", accountId);
			throw new AccountClosureFailedException(accountId, account.getStatus(), account.getBalance());
		}

	}

	//Helper Methods

	public void checkAccountStatus(AccountEntity account) {
		if (account.getStatus() != AccountStatus.ACTIVE) {
			log.error("The status of account with id {} is not valid for transfers and transactions", account.getId());
			throw new AccountOperationNotAllowedException(account.getId(), account.getStatus());
		}

	}

	public void updateBalance(AccountEntity account, BigDecimal amount, TransactionType transactionType) {

		boolean incoming = transactionType == TransactionType.DEPOSIT
				|| transactionType == TransactionType.TRANSFER_IN;

		if (incoming) {
			account.setBalance(account.getBalance().add(amount));
		} else {
			account.setBalance(account.getBalance().subtract(amount));
		}

		accountRepository.saveAndFlush(account);
		log.info("Balance for Account with id {} has been updated", account.getId());
	}

	private String generateAccountNumber() {

		return "ACC-" + UUID.randomUUID()
				.toString()
				.substring(0, 12)
				.toUpperCase();
	}

	private AccountResponse responseBuilder(UserProfileEntity ownerProfile, AccountEntity account) {

		return AccountResponse.builder()

				.id(account.getId())
				.name(ownerProfile.getName())
				.surname(ownerProfile.getSurname())
				.accountNumber(account.getAccountNumber())
				.currency(account.getCurrency())
				.balance(account.getBalance())
				.currency(account.getCurrency())
				.status(account.getStatus())
				.build();
	}

	//Overloaded for Customers
	private AccountResponse responseBuilder(AccountEntity account) {

		return AccountResponse.builder()
				.id(account.getId())
				.accountNumber(account.getAccountNumber())
				.balance(account.getBalance())
				.currency(account.getCurrency())
				.status(account.getStatus())
				.build();
	}

	public List<AccountEntity> getAllAccountsByUser(UserEntity user) {
		return accountRepository.findByOwner(user).stream().toList();
	}

	private void validStatusTransition(AccountStatus currentStatus, AccountStatusRequest newStatus) {
		boolean validTransition = (currentStatus == AccountStatus.ACTIVE && newStatus.getStatus() == AccountStatus.BLOCKED)
				|| (currentStatus == AccountStatus.BLOCKED && newStatus.getStatus() == AccountStatus.ACTIVE);

		if (!validTransition) {
			throw new AccountInvalidStatusTransitionException(currentStatus, newStatus.getStatus());
		}
	}

	private boolean balanceIsZero(AccountEntity account) {
		return account.getBalance().compareTo(BigDecimal.ZERO) == 0;
	}

	private boolean accountIsActive(AccountEntity account) {
		return account.getStatus() == AccountStatus.ACTIVE;
	}

	public boolean userIsOwner(AccountEntity account, AuthenticatedUser authenticatedUser) {
		return account.getOwner().getId().equals(authenticatedUser.getUserId());
	}
}