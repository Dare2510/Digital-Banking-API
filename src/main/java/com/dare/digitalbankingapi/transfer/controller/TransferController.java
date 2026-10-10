package com.dare.digitalbankingapi.transfer.controller;

import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.dare.digitalbankingapi.transfer.dto.DepositAndWithdrawalRequest;
import com.dare.digitalbankingapi.transfer.dto.DepositAndWithdrawalResponse;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/transfers")
@RequiredArgsConstructor
public class TransferController {

	private final TransferService transferService;

	@PostMapping("/transfer")
	public ResponseEntity<TransferResponse> createTransfer(@RequestBody TransferRequest transferRequest) {
		return ResponseEntity.ok(transferService.createTransferBetweenAccounts(transferRequest));
	}

	@PostMapping("/{transactionType}")
	public ResponseEntity<DepositAndWithdrawalResponse> depositAndWithdrawal(@RequestBody DepositAndWithdrawalRequest depositAndWithdrawalRequest, @PathVariable TransactionType transactionType) {
		return ResponseEntity.ok(transferService.depositAndWithdrawal(depositAndWithdrawalRequest, transactionType));
	}


}
