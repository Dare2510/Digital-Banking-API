package com.dare.digitalbankingapi.account.controller;

import com.dare.digitalbankingapi.account.dto.AccountRequest;
import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
		return ResponseEntity.ok(accountService.createAccount(accountRequest));
	}

}
