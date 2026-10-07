package com.dare.digitalbankingapi.transaction.entity;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.transfer.entity.TransferEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transaction")
@NoArgsConstructor
@Getter
@Setter
public class TransactionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TransactionType type;

	private BigDecimal amount;

	private BigDecimal balanceAfter;

	private String reference;

	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private AccountEntity account;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transfer_id")
	private TransferEntity transfer;
}
