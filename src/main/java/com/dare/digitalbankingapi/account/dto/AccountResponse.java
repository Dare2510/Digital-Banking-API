package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

	private Long id;
	private String name;
	private String surname;
	private String accountNumber;
	private BigDecimal balance;
	private Currency currency;
	private AccountStatus status;

}
