package com.dare.digitalbankingapi.staff.controller;

import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff")
public class StaffController {

	private final AccountService accountService;
	private final TransactionService transactionService;

	@PatchMapping("/accounts/{accountId}/{accountStatus}")
	public ResponseEntity<Void> updateAccountStatus(@PathVariable Long accountId, @PathVariable AccountStatus accountStatus) {

		accountService.changeAccountStatus(accountId, accountStatus);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/accounts/deposits")
	public ResponseEntity<TransactionResponse> deposit(@RequestBody TransactionRequest depositRequest) {
		return ResponseEntity.ok(transactionService.deposit(depositRequest));
	}

	@GetMapping("/accounts")
	public ResponseEntity<Page<AccountResponse>> getAllAccounts(@PageableDefault(
			size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable) {

		return ResponseEntity.ok(accountService.viewAllAccounts(pageable));
	}
}
