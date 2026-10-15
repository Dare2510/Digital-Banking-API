package com.dare.digitalbankingapi.transfer.controller;

import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/transfers")
@RequiredArgsConstructor
public class TransferController {

	private final TransferService transferService;

	@PostMapping("/")
	public ResponseEntity<TransferResponse> createTransferBetweenAccounts(@RequestBody TransferRequest transferRequest) {
		return ResponseEntity.ok(transferService.createTransferBetweenAccounts(transferRequest));
	}

	@GetMapping("/{transferId}")
	public ResponseEntity<TransferResponse> getTransferDetailsById(@PathVariable Long transferId) {
		return ResponseEntity.ok(transferService.viewTransferDetails(transferId));
	}

	@GetMapping("/all/{accountId}")
	public ResponseEntity<List<TransferResponse>> getTransferDetailsByAccountId(@PathVariable Long accountId) {
		return ResponseEntity.ok(transferService.findByAccountId(accountId));
	}


}
