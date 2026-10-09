package com.dare.digitalbankingapi.transfer.controller;

import com.dare.digitalbankingapi.transfer.dto.DepositRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferRequest;
import com.dare.digitalbankingapi.transfer.dto.TransferResponse;
import com.dare.digitalbankingapi.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/transfers")
@RequiredArgsConstructor
public class TransferController {

	private final TransferService transferService;

	@PostMapping("/transfer")
	public ResponseEntity<TransferResponse> createTransfer(@RequestBody TransferRequest transferRequest){
		return ResponseEntity.ok(transferService.createTransferBetweenAccounts(transferRequest));
	}

	@PostMapping("/deposit")
	public ResponseEntity<TransferResponse> depositTransfer(@RequestBody DepositRequest depositRequest){
		return ResponseEntity.ok(transferService.deposit(depositRequest));
	}


}
