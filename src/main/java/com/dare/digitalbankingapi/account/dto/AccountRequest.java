package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

	Long userId;
	Currency currency;

}
