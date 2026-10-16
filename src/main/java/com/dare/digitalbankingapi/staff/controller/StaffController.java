package com.dare.digitalbankingapi.staff.controller;

import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.service.AccountService;
import com.dare.digitalbankingapi.transaction.dto.TransactionRequest;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import com.dare.digitalbankingapi.user.dto.UserDetailsResponse;
import com.dare.digitalbankingapi.user.service.UserService;
import jakarta.validation.Valid;
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
	private final TransferService transferService;
	private final UserService userService;

	//Account Endpoints

	@PatchMapping("/accounts/{accountId}/{accountStatus}")
	public ResponseEntity<Void> updateAccountStatus(@PathVariable Long accountId, @PathVariable AccountStatus accountStatus) {

		accountService.changeAccountStatus(accountId, accountStatus);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/accounts/deposits")
	public ResponseEntity<TransactionResponse> depositFromAccount(@Valid @RequestBody TransactionRequest depositRequest) {
		return ResponseEntity.ok(transactionService.deposit(depositRequest));
	}

	@GetMapping("/accounts")
	public ResponseEntity<Page<AccountResponse>> getPageOfAllAccounts(@PageableDefault(
			size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

		return ResponseEntity.ok(accountService.viewAllAccounts(pageable));
	}

	@GetMapping("/accounts/{accountId}")
	public ResponseEntity<AccountResponse> getAccountDetailsForSpecificAccountId(@PathVariable Long accountId) {
		return ResponseEntity.ok(accountService.viewAccountDetails(accountId));
	}

	//User Endpoints

	@GetMapping("/users")
	public ResponseEntity<Page<UserDetailsResponse>> getPageOfAllUsersWithDetails(@PageableDefault(
			size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(userService.getPageOfUsersWithDetails(pageable));
	}

	//Transfer Endpoints

	@GetMapping("/transfers")
	public ResponseEntity<Page<TransferResponse>> getPageAllTransfers(@PageableDefault(
			size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(transferService.viewAllTransfers(pageable));
	}

	//Transaction Endpoints

	@GetMapping("/transactions")
	public ResponseEntity<Page<TransactionResponse>> getPageAllTransactions(@PageableDefault(
			size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(transactionService.viewAllTransactions(pageable));
	}
}
