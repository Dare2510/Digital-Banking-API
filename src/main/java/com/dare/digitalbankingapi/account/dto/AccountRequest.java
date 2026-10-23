package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

	@NotNull
	@Positive(message = "User id must be > 0")
	private Long userId;

	private Currency currency;

}
