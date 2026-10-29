package com.dare.digitalbankingapi.service;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.account.repository.AccountRepository;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.common.validation.SortValidator;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserProfileEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.service.UserProfileService;
import com.dare.digitalbankingapi.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

	private AccountService accountService;

	@Mock
	private UserProfileService userProfileService;

	@Mock
	private UserService userService;

	@Mock
	private AccountRepository accountRepository;

	@Spy
	private SortValidator sortValidator;

	//Fields

	private final Long ACCOUNT_ID = 1L;
	private final String ACCOUNT_NUMBER = "123456789";

	//Helper
	private UserProfileEntity returnUserProfileEntity() {
		return new UserProfileEntity(
				returnUserEntity(),
				"testName", "testSurName", "testStreet",
				"99", "testCity", "testZipCode", "testCountry"
		);
	}

	private UserEntity returnUserEntity() {
		UserEntity customer =  new UserEntity(
				"testEmail@mail.com",
				"passwordHash",
				UserRole.CUSTOMER
		);

		customer.setId(1L);
		return customer;

	}

	private AccountRequest returnAccountRequest() {
		return new AccountRequest(
				Currency.EUR
		);
	}

	private AuthenticatedUser returnAuthenticatedUser(UserEntity user) {
		return new AuthenticatedUser(user.getId(),user.getEmail(),user.getUserRole());
	}

	private void setAccountValues(AccountEntity accountEntity,UserEntity userEntity,AccountRequest accountRequest) {
		accountEntity.setId(ACCOUNT_ID);
		accountEntity.setOwner(userEntity);
		accountEntity.setAccountNumber(ACCOUNT_NUMBER);
		accountEntity.setCurrency(accountRequest.getCurrency());
		accountEntity.setStatus(AccountStatus.ACTIVE);
		accountEntity.setCreatedAt(LocalDateTime.now());


	}

	@BeforeEach
	public void setup() {
		accountService = new AccountService(accountRepository,userProfileService,userService,sortValidator);
	}

	@Test
	public void createAccountTest_whenUserAndProfileExists_returnFullAccountResponse() {
		UserEntity customer = returnUserEntity();
		UserProfileEntity customerProfile = returnUserProfileEntity();
		AuthenticatedUser authenticatedUser = returnAuthenticatedUser(customer);
		AccountRequest accountRequest = returnAccountRequest();

		when(userService.getUserById(authenticatedUser.getUserId())).thenReturn(customer);
		when(userProfileService.getProfileEntity(authenticatedUser.getUserId())).thenReturn(customerProfile);

		when(accountRepository.saveAndFlush(any(AccountEntity.class))).
				thenAnswer(i -> {
					AccountEntity newAccount = i.getArgument(0);
					setAccountValues(newAccount,customer,accountRequest);
					return newAccount;
						});

		AccountResponse response = accountService.createAccount(authenticatedUser, returnAccountRequest());

		assertEquals(ACCOUNT_ID, response.getId());
		assertEquals(customerProfile.getName(),response.getName());
		assertEquals(customerProfile.getSurname(),response.getSurname());
		assertEquals(ACCOUNT_NUMBER,response.getAccountNumber());
		assertEquals(BigDecimal.ZERO,response.getBalance());
		assertEquals(accountRequest.getCurrency(),response.getCurrency());
		assertEquals(AccountStatus.ACTIVE,response.getStatus());

		verify(userService, times(1)).getUserById(authenticatedUser.getUserId());
		verify(userProfileService, times(1)).getProfileEntity(authenticatedUser.getUserId());
		verify(accountRepository, times(1)).saveAndFlush(any(AccountEntity.class));
	}

	@Test
	public void viewAccountDetails_whenCustomerIsNotOwner_throwsAccountNotFoundException() {
		UserEntity loggedInCustomer = returnUserEntity();
		UserEntity owner = new UserEntity();
		owner.setId(2L);

		AuthenticatedUser authenticatedUser = returnAuthenticatedUser(loggedInCustomer);

		AccountEntity notOwningAccount = new AccountEntity();
		notOwningAccount.setOwner(owner);

		assertThatThrownBy(() -> accountService.viewAccountDetails(authenticatedUser,ACCOUNT_ID))
		.isInstanceOf(AccountNotFoundException.class)
				.hasMessage("Account with id " + ACCOUNT_ID + " not found");

		verify(accountRepository,times(1)).findById(ACCOUNT_ID);

	}
}
