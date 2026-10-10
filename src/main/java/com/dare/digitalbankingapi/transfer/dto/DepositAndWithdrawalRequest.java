package com.dare.digitalbankingapi.transfer.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class DepositAndWithdrawalRequest {

	@Positive(message = "From Account id must be > 0")
	private Long accountId;

	@Positive(message = "Amount id must be > 0")
	private BigDecimal amount;
}
