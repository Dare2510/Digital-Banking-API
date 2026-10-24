package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

	@NotNull(message = "Currency is required")
	private Currency currency;

}
