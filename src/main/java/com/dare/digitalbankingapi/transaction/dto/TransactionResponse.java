package com.dare.digitalbankingapi.transaction.dto;

import com.dare.digitalbankingapi.transaction.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

	private Long id;
	private LocalDateTime createdDate;
	private Long fromAccountId;
	private Long toAccountId;
	private TransactionType transactionType;
	private String reference;
	private BigDecimal amount;
	private BigDecimal balanceAfter;


}
