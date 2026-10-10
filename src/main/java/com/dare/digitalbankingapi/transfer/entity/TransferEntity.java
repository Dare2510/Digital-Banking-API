package com.dare.digitalbankingapi.transfer.entity;

import com.dare.digitalbankingapi.account.entity.AccountEntity;
import com.dare.digitalbankingapi.account.entity.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "transfers")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class TransferEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_account_id")
	private AccountEntity fromAccount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_account_id")
	private AccountEntity toAccount;

	@Enumerated(EnumType.STRING)
	private Currency currency;

	@Enumerated(EnumType.STRING)
	private TransferStatus status;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public TransferEntity(AccountEntity fromAccount, AccountEntity toAccount, BigDecimal amount, Currency currency) {
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.amount = amount;
		this.currency = currency;
		this.status = TransferStatus.CREATED;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public TransferEntity(AccountEntity fromAccount, BigDecimal amount, Currency currency) {
		this.fromAccount = fromAccount;
		this.toAccount = fromAccount;
		this.amount = amount;
		this.currency = currency;
		this.status = TransferStatus.CREATED;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}


}
