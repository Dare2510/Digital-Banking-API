package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.Currency;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

	@NotNull(message = "Currency is required")
	private Currency currency;

}
