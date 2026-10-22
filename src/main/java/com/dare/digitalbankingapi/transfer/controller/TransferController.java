package com.dare.digitalbankingapi.transfer.controller;

import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/customer/transfers")
@RequiredArgsConstructor
public class TransferController {

	private final TransferService transferService;

	@PostMapping
	public ResponseEntity<TransferResponse> createTransferBetweenAccounts(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
																		  @Valid @RequestBody TransferRequest transferRequest) {
		return ResponseEntity.ok(transferService.createTransferBetweenAccounts(authenticatedUser,transferRequest));
	}

	@GetMapping("/{transferId}")
	public ResponseEntity<TransferResponse> getTransferDetailsById(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
																   @PathVariable Long transferId) {
		return ResponseEntity.ok(transferService.viewTransferDetails(authenticatedUser,transferId));
	}

	@GetMapping("/all/{accountId}")
	public ResponseEntity<List<TransferResponse>> getTransferDetailsByAccountId(
			@AuthenticationPrincipal AuthenticatedUser authenticatedUser,@PathVariable Long accountId) {
		return ResponseEntity.ok(transferService.findByAccountId(authenticatedUser,accountId));
	}


}
