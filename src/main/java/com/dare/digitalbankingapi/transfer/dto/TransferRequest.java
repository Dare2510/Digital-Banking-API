package com.dare.digitalbankingapi.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TransferRequest {

	@NotNull(message = "From Account is required")
	@Positive(message = "From Account id must be > 0")
	private Long fromAccountId;

	@NotNull(message = "To Account is required")
	@Positive(message = "To Account id must be > 0")
	private Long toAccountId;

	@NotNull(message = "Amount is required")
	@Positive(message = "Amount id must be > 0")
	@Digits(integer = 17, fraction = 2,
			message = "Amount must have at most 2 decimal places")
	private BigDecimal amount;

	@NotBlank(message = "Reference is required")
	@Size(max = 30, message = "Reference must not exceed 30 characters")
	private String reference;
}
