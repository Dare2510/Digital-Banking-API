package com.dare.digitalbankingapi.account.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@NoArgsConstructor
@Getter
@Setter
public class AccountEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	private String accountNumber;

	private BigDecimal balance;

	//private User owner;

	private Currency currency;

	private AccountStatus status;

	@Version
	private Long version;

	private LocalDateTime createdAt;
}
