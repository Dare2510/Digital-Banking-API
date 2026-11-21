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

	public String getMessage() {
		return "Account with id " + accountId
				+ " and account number " + accountNumber
				+ " has been closed";
	}
}
