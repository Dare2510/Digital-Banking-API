package com.dare.digitalbankingapi.account.controller;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/customer/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;
	private final TransactionService transactionService;

	//Customer

	@PostMapping
	public ResponseEntity<AccountResponse> createNewAccount(@Valid @RequestBody AccountRequest accountRequest) {

		return ResponseEntity.ok(accountService.createAccount(accountRequest));
	}

	@GetMapping("/all")
	public ResponseEntity<List<AccountResponse>> getAllAccountsByUserId(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

		return ResponseEntity.ok(accountService.viewAllAccountsByUser(authenticatedUser));
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<AccountResponse> getAccountById(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
														  @PathVariable Long accountId) {

		return ResponseEntity.ok(accountService.viewAccountDetails(authenticatedUser,accountId));
	}

	@PostMapping("/withdrawals")
	public ResponseEntity<TransactionResponse> withdrawFromAccount(@Valid @RequestBody TransactionRequest transactionRequest) {
		return ResponseEntity.ok(transactionService.withdraw(transactionRequest));
	}

	@PostMapping("/{accountId}/close")
	public ResponseEntity<Void> closeAccount(@PathVariable Long accountId) {
		accountService.closeAccount(accountId);
		return ResponseEntity.noContent().build();
	}
}