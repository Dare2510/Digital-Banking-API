package com.dare.digitalbankingapi.account.service;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserProfileService userProfileService;
	private final UserService userService;


	public AccountResponse createAccount(AccountRequest accountRequest) {
		Long userId = accountRequest.getUserId();

		UserEntity owner = userService.getUserById(accountRequest.getUserId());
		UserProfileEntity ownerProfile = userProfileService.getProfileEntity(userId);

		AccountEntity newAccount = new AccountEntity(
				owner,
				generateAccountNumber(),
				accountRequest.getCurrency()
		);

		accountRepository.save(newAccount);

		return responseBuilder(ownerProfile, newAccount);

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
				account.getCurrency().toString(),
				account.getStatus().toString()
		);
	}

	public AccountEntity getAccount(Long accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(
						() -> {
							log.info("Account with id: {} not found", accountId);
							return new AccountNotFoundException(accountId);
						}
				);

	}


}
