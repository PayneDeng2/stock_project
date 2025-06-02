package com.haydenshui.stock.lib.entity.account;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a capital account transaction.
 * <p>
 * This class records details of all transactions that occur on a capital account,
 * including deposits, withdrawals, transfers, and trading-related transactions.
 * </p>
 * 
 * @author Hzeristo
 * @version 1.0
 * @since 2025-04-20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "capital_account_transaction", indexes = {
        @Index(name = "idx_cat_capital_account_id", columnList = "capital_account_id"),
        @Index(name = "idx_cat_transaction_time", columnList = "transaction_time"),
        @Index(name = "idx_cat_type", columnList = "type")
})
public class CapitalAccountTransaction {

    /**
     * Unique identifier for the transaction record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The capital account associated with this transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capital_account_id", nullable = false)
    private CapitalAccount capitalAccount;

    /**
     * The type of transaction (e.g., deposit, withdrawal, transfer, trade payment).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    /**
     * The amount involved in the transaction.
     */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * The balance of the capital account after this transaction.
     */
    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    /**
     * A description or note for the transaction.
     */
    @Column(name = "description")
    private String description;

    /**
     * The reference ID for external transactions (e.g., trade order ID, bank transfer ID).
     */
    @Column(name = "reference_id")
    private String referenceId;

    /**
     * The time when the transaction occurred.
     */
    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    /**
     * Creation timestamp of the transaction record.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp of the transaction record.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
