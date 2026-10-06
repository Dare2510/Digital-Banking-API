package com.dare.digitalbankingapi.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

	String name;
	String surname;
	String accountNumber;
	String currency;
	String status;

}
