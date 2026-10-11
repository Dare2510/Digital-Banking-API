package com.dare.digitalbankingapi.transaction.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {

	private Long fromAccountId;
	private BigDecimal amount;
	private BigDecimal balanceAfter;

}
