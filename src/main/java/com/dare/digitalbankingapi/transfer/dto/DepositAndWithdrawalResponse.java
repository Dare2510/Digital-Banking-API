package com.dare.digitalbankingapi.transfer.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositAndWithdrawalResponse {

	private Long fromAccountId;
	private BigDecimal amount;
	private BigDecimal balanceAfter;

}
