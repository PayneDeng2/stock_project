package com.haydenshui.stock.lib.entity.account;

/**
 * Enumeration representing different types of capital account transactions.
 * <p>
 * This enum defines the various types of transactions that can occur on a capital account,
 * such as deposits, withdrawals, trades, and transfers.
 * </p>
 * 
 * @author Hzeristo
 * @version 1.0
 * @since 2025-04-20
 */
public enum TransactionType {
    
    /**
     * Represents a deposit into the capital account.
     */
    DEPOSIT("存款"),
    
    /**
     * Represents a withdrawal from the capital account.
     */
    WITHDRAWAL("取款"),
    
    /**
     * Represents a payment for buying stocks.
     */
    BUY_PAYMENT("买入支付"),
    
    /**
     * Represents a receipt from selling stocks.
     */
    SELL_RECEIPT("卖出收入"),
    
    /**
     * Represents a commission fee charged for trading.
     */
    COMMISSION_FEE("佣金"),
    
    /**
     * Represents a transfer to another account.
     */
    TRANSFER_OUT("转出"),
    
    /**
     * Represents a transfer from another account.
     */
    TRANSFER_IN("转入"),
    
    /**
     * Represents a dividend payment.
     */
    DIVIDEND("股息"),
    
    /**
     * Represents freezing of funds for a pending transaction.
     */
    FREEZE("资金冻结"),
    
    /**
     * Represents unfreezing of previously frozen funds.
     */
    UNFREEZE("资金解冻"),
    
    /**
     * Represents interest earned on the account balance.
     */
    INTEREST("利息"),
    
    /**
     * Represents a tax payment.
     */
    TAX("税费"),
    
    /**
     * Represents an adjustment to the account balance.
     */
    ADJUSTMENT("账户调整"),
    
    /**
     * Represents other types of transactions not covered above.
     */
    OTHER("其他");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    /**
     * Gets the description of this transaction type.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines if the transaction type is a credit (increases balance).
     * 
     * @return true if the transaction is a credit, false otherwise
     */
    public boolean isCredit() {
        return this == DEPOSIT || this == SELL_RECEIPT || this == TRANSFER_IN || 
               this == DIVIDEND || this == UNFREEZE || this == INTEREST;
    }
    
    /**
     * Determines if the transaction type is a debit (decreases balance).
     * 
     * @return true if the transaction is a debit, false otherwise
     */
    public boolean isDebit() {
        return this == WITHDRAWAL || this == BUY_PAYMENT || this == COMMISSION_FEE || 
               this == TRANSFER_OUT || this == FREEZE || this == TAX;
    }
    
    /**
     * Determines if the transaction type affects the available balance.
     * 
     * @return true if the transaction affects the available balance, false otherwise
     */
    public boolean affectsAvailableBalance() {
        return this != FREEZE && this != UNFREEZE;
    }
}
