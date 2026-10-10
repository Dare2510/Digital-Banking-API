package com.dare.digitalbankingapi.account.controller;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
		return ResponseEntity.ok(accountService.createAccount(accountRequest));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<List<AccountResponse>> findAll(@PathVariable Long userId) {
		return ResponseEntity.ok().body(accountService.viewAllAccounts(userId));
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<AccountResponse> getAccount(@PathVariable("accountId") Long accountId) {
		return ResponseEntity.ok(accountService.viewAccountDetails(accountId));
	}

}
