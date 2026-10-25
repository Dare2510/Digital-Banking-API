package com.dare.digitalbankingapi.account.dto;

import com.dare.digitalbankingapi.account.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusRequest {

	@NotNull(message = "Status is required")
	private AccountStatus status;
}
