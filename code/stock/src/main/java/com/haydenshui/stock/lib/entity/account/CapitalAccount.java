package com.haydenshui.stock.lib.entity.account;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.haydenshui.stock.lib.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract entity representing a capital account.
 * <p>
 * This class defines common attributes and behaviors for different types of capital accounts.
 * It uses a joined inheritance strategy to allow for subclassing while maintaining data normalization.
 * </p>
 * 
 * @author YourName
 * @version 1.0
 * @since 2025-03-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "capital_account", indexes = {
    @Index(name = "idx_capital_account_number", columnList = "capital_account_number"),
    @Index(name = "idx_securities_account_id", columnList = "securities_account_id")
})
public class CapitalAccount {

    /**
     * Unique identifier for the capital account.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique capital account number.
     */
    @Column(name = "capital_account_number", nullable = false, unique = true)
    private String capitalAccountNumber;

    /**
     * Type of the capital account (e.g., cash account, margin account).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CapitalAccountType type;

    /**
     * Total balance of the capital account.
     */
    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = new BigDecimal("0.00");

    /**
     * Available balance that can be used for transactions.
     */
    @Column(name = "available_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal availableBalance = new BigDecimal("0.00");

    /**
     * Frozen balance that cannot be used for transactions.
     */
    @Column(name = "frozen_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal frozenBalance = new BigDecimal("0.00");

    /**
     * Currency type for this capital account.
     */
    @Column(name = "currency", nullable = false)
    private String currency;

    /**
     * Name of the bank associated with this capital account.
     */
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    /**
     * Unique bank account number linked to this capital account.
     */
    @Column(name = "bank_account_number", nullable = false)
    private String bankAccountNumber;

    /**
     * Encrypted password for securing the capital account.
     * <p>
     * The password must be a six-digit numeric value. It is stored as a hashed value using BCrypt.
     * </p>
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Status of the capital account (e.g., ACTIVE, SUSPENDED, CLOSED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * The securities account id associated with this capital account.
     */
    @Column(name = "securities_account_id", nullable = false)
    private Long securitiesAccountId;
    
    /**
     * Creation timestamp of the capital account.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp of the capital account.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * The account's transaction histories.
     */
    @OneToMany(mappedBy = "capitalAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CapitalAccountTransaction> transactions = new ArrayList<>();
    
    /**
     * Freezes a specified amount from the available balance.
     * <p>
     * This method reduces the available balance by the specified amount and adds
     * the same amount to the frozen balance. This is typically used when funds need
     * to be reserved for a pending transaction but should not yet be removed from the
     * total balance.
     * </p>
     * 
     * @param amount The amount to be frozen
     * @throws IllegalArgumentException if amount is less than or equal to zero
     * @throws InsufficientBalanceException if available balance is less than the amount
     */
    public void freezeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Freeze amount must be greater than zero");
        }
        
        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient available balance for freezing");
        }
        
        availableBalance = availableBalance.subtract(amount);
        frozenBalance = frozenBalance.add(amount);
    }
    
    /**
     * Unfreezes a specified amount and adds it back to the available balance.
     * <p>
     * This method reduces the frozen balance by the specified amount and adds
     * the same amount to the available balance. This is typically used when a
     * pending transaction is canceled or completed, releasing the previously
     * reserved funds.
     * </p>
     * 
     * @param amount The amount to be unfrozen
     * @throws IllegalArgumentException if amount is less than or equal to zero or greater than frozen balance
     */
    public void unfreezeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unfreeze amount must be greater than zero");
        }
        
        if (frozenBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Cannot unfreeze more than the frozen amount");
        }
        
        frozenBalance = frozenBalance.subtract(amount);
        availableBalance = availableBalance.add(amount);
    }
    
    /**
     * Deposits money into the capital account.
     * <p>
     * This method increases both the total balance and the available balance
     * by the specified amount. This is used when money is added to the account,
     * such as from a bank transfer or payment.
     * </p>
     * 
     * @param amount The amount to deposit
     * @throws IllegalArgumentException if amount is less than or equal to zero
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        
        balance = balance.add(amount);
        availableBalance = availableBalance.add(amount);
    }
    
    /**
     * Withdraws money from the capital account.
     * <p>
     * This method decreases both the total balance and the available balance
     * by the specified amount. This is used when money is removed from the account,
     * such as for a bank transfer or payment.
     * </p>
     * 
     * @param amount The amount to withdraw
     * @throws IllegalArgumentException if amount is less than or equal to zero
     * @throws InsufficientBalanceException if available balance is less than the amount
     */
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be greater than zero");
        }
        
        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient available balance for withdrawal");
        }
        
        balance = balance.subtract(amount);
        availableBalance = availableBalance.subtract(amount);
    }
    
    /**
     * Transfers money from this capital account to another capital account.
     * <p>
     * This method withdraws the specified amount from the current account and
     * deposits it into the target account. It performs validation checks to ensure
     * that the withdrawal is possible before proceeding with the transfer.
     * </p>
     * 
     * @param targetAccount The target capital account
     * @param amount The amount to transfer
     * @throws IllegalArgumentException if amount is less than or equal to zero
     * @throws InsufficientBalanceException if available balance is less than the amount
     */
    public void transferTo(CapitalAccount targetAccount, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        
        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient available balance for transfer");
        }
        
        // 扣除当前账户金额
        withdraw(amount);
        
        // 增加目标账户金额
        targetAccount.deposit(amount);
    }
    
    /**
     * Creates a transaction record for this capital account.
     * <p>
     * This method creates a new transaction record associated with this capital account,
     * recording details such as the transaction type, amount, and a description. It also
     * captures the current balance of the account after the transaction is completed.
     * The created transaction is added to the account's transaction history.
     * </p>
     * 
     * @param type The transaction type
     * @param amount The transaction amount
     * @param description The transaction description
     * @return The created transaction record
     */
    public CapitalAccountTransaction createTransaction(TransactionType type, BigDecimal amount, String description) {
        CapitalAccountTransaction transaction = new CapitalAccountTransaction();
        transaction.setCapitalAccount(this);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfter(this.balance);
        transaction.setTransactionTime(LocalDateTime.now());
        
        this.transactions.add(transaction);
        return transaction;
    }
}