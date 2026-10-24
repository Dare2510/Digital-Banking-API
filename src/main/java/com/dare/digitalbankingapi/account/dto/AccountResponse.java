package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.AccountStatus;
import com.dare.digitalbankingapi.account.entity.Currency;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

	private Long id;
	private String name;
	private String surname;
	private String accountNumber;
	private BigDecimal balance;
	private Currency currency;
	private AccountStatus status;

}
