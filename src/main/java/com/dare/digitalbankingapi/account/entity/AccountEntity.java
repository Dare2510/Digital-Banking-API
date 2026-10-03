package com.dare.digitalbankingapi.account.entity;

import com.dare.digitalbankingapi.user.entity.UserEntity;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;

	private String accountNumber;

	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	private Currency currency;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@Version
	private Long version;

	private LocalDateTime createdAt;
}
