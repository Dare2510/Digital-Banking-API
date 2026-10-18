package com.dare.digitalbankingapi.account.controller;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;
	private final TransactionService transactionService;

	//Customer

	@PostMapping
	public ResponseEntity<AccountResponse> createNewAccount(@Valid @RequestBody AccountRequest accountRequest) {

		return ResponseEntity.ok(accountService.createAccount(accountRequest));
	}

	@GetMapping("/all/{userId}")
	public ResponseEntity<List<AccountResponse>> getAllAccountsByUserId(@PathVariable Long userId) {

		return ResponseEntity.ok(accountService.viewAllAccountsByUser(userId));
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId) {

		return ResponseEntity.ok(accountService.viewAccountDetails(accountId));
	}

	@PostMapping("/withdraw")
	public ResponseEntity<TransactionResponse> withdrawFromAccount(@RequestBody TransactionRequest transactionRequest) {
		return ResponseEntity.ok(transactionService.withdraw(transactionRequest));
	}

	@PatchMapping("/{accountId}/close")
	public ResponseEntity<Void> closeAccount(@PathVariable Long accountId) {
		accountService.closeAccount(accountId);
		return ResponseEntity.noContent().build();
	}
}