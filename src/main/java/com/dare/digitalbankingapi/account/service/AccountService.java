package com.dare.digitalbankingapi.account.service;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.account.exceptions.AccountOperationNotAllowedException;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.common.validation.SortValidator;
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
			"accountNumber",
			"balance",
			"currency",
			"status",
			"createdAt"
	);

	@Transactional
	public AccountResponse createAccount(AccountRequest accountRequest) {

		Long userId = accountRequest.getUserId();

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

	public AccountResponse viewAccountDetails(Long accountId) {

		AccountEntity account = getAccount(accountId);

		UserProfileEntity userProfile = userProfileService.getProfileEntity(account.getOwner().getId());

		return responseBuilder(userProfile, account);
	}

	public List<AccountResponse> viewAllAccountsByUser(Long userId) {

		UserEntity owner = userService.getUserById(userId);

		UserProfileEntity userProfile = userProfileService.getProfileEntity(userId);

		log.info("Viewing all accounts with user id {}", owner.getId());

		return accountRepository.findByOwner(owner)
				.stream()
				.map(account ->
						responseBuilder(userProfile, account))
				.toList();
	}

	public Page<AccountResponse> viewAllAccounts(Pageable pageable) {
		sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

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

	public void changeAccountStatus(Long accountId, AccountStatus accountStatus) {
		AccountEntity account = getAccount(accountId);

		account.setStatus(accountStatus);
		accountRepository.saveAndFlush(account);

	}

	//Helper Methods

	public void checkAccountStatus(AccountEntity account, AccountStatus accountStatus) {
		boolean statusIsValid = accountStatus == AccountStatus.ACTIVE || accountStatus == AccountStatus.UNDER_REVIEW;

		if (!statusIsValid) {
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

		return new AccountResponse(
				ownerProfile.getName(),
				ownerProfile.getSurname(),
				account.getAccountNumber(),
				account.getBalance(),
				account.getCurrency(),
				account.getStatus()
		);
	}
}