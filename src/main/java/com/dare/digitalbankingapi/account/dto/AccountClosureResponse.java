package com.dare.digitalbankingapi.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountClosureResponse {

	private Long accountId;
	private String accountNumber;
	private final String message = "Account with " + accountId +
			" and " + accountNumber + "Account number has been closed";
}
