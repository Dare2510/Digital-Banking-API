package com.dare.digitalbankingapi.transaction.controller;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transaction.dto.TransactionResponse;
import com.dare.digitalbankingapi.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@GetMapping
	public ResponseEntity<List<TransactionResponse>> getAllTransactionForUser(
			@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

		return ResponseEntity.ok(transactionService.getAllTransactionsByAuthenticatedUser(authenticatedUser));
	}

	@GetMapping("/{transactionId}")
	public ResponseEntity<TransactionResponse> getTransactionById(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
																  @PathVariable Long transactionId) {
		return ResponseEntity.ok(transactionService.findTransactionDetailsById(authenticatedUser, transactionId));
	}
}
