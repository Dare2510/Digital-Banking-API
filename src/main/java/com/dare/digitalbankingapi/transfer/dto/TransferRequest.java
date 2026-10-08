package com.dare.digitalbankingapi.transfer.dto;

import com.dare.digitalbankingapi.account.entity.Currency;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TransferRequest {

	@Positive(message = "From Account id must be > 0")
	private Long fromAccountId;

	@Positive(message = "To Account id must be > 0")
	private Long toAccountId;

	@Positive(message = "Amount id must be > 0")
	private BigDecimal amount;

	private Currency currency;
}
