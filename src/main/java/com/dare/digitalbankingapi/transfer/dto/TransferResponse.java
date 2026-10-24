package com.dare.digitalbankingapi.transfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferResponse {

	private Long fromAccountId;
	private Long toAccountId;
	private Long transferId;
	private BigDecimal amount;
	private String reference;

}
