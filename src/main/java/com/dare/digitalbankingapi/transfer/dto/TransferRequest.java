package com.dare.digitalbankingapi.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TransferRequest {

	@NotNull
	@Positive(message = "From Account id must be > 0")
	private Long fromAccountId;

	@NotNull
	@Positive(message = "To Account id must be > 0")
	private Long toAccountId;

	@NotNull
	@Positive(message = "Amount id must be > 0")
	private BigDecimal amount;

	@NotBlank(message = "Reference is required")
	@Size(max = 30, message = "Reference must not exceed 30 characters")
	private String reference;
}
