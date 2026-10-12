package com.dare.digitalbankingapi.staff.controller;

import com.dare.digitalbankingapi.account.dto.AccountResponse;
import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff")
public class StaffController {

	private final AccountService accountService;

	@PatchMapping("/accounts/{id}/{accountStatus}")
	public ResponseEntity<Void> updateAccountStatus(@PathVariable Long id, @PathVariable AccountStatus accountStatus){

		accountService.changeAccountStatus(id, accountStatus);

		return ResponseEntity.noContent().build();
	}
}
