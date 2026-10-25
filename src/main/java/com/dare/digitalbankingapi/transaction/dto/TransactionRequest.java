package com.dare.digitalbankingapi.transaction.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TransactionRequest {

	@NotNull(message = "From Account id is required")
	@Positive(message = "From Account id must be > 0")
	private Long accountId;

	@NotNull
	@Positive(message = "Amount id must be > 0")
	@Digits(integer = 17, fraction = 2,
			message = "Amount must have at most 2 decimal places")
	private BigDecimal amount;
}
