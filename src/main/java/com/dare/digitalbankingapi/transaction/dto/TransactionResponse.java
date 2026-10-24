package com.dare.digitalbankingapi.transaction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

	private Long fromAccountId;
	private Long toAccountId;
	private String reference;
	private BigDecimal amount;
	private BigDecimal balanceAfter;


}
