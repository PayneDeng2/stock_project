package com.haydenshui.stock.lib.entity.position;

import com.haydenshui.stock.lib.entity.account.SecuritiesAccount;
import com.haydenshui.stock.lib.entity.stock.Stock;
import com.haydenshui.stock.lib.entity.trade.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a position in a securities account, tracking stock holdings.
 * <p>
 * This entity keeps track of the number of shares held in an account, 
 * along with the average purchase price. It is updated whenever a trade 
 * is executed.
 * </p>
 * 
 * <p>
 * A position is uniquely defined by the combination of a securities account 
 * and a stock. It allows users to track how many shares they hold and the 
 * average price at which the shares were acquired. Each time a trade is made 
 * involving a stock, the position is updated to reflect the new holdings and 
 * the new average price.
 * </p>
 *
 * @author Hzeristo
 * @version 1.1
 * @since 2025-03-04
 * @see SecuritiesAccount
 * @see Stock
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"stopLossTakeProfitRules"})
@Entity
@Table(name = "position", uniqueConstraints = {
        @UniqueConstraint(name = "uq_position_account_stock", columnNames = {"securities_account_id", "stock_code"})
}, indexes = {
        @Index(name = "idx_position_securities_account_id", columnList = "securities_account_id"),
<<<<<<< HEAD
=======
        @Index(name = "idx_position_stock_id", columnList = "stock_id"),
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
        @Index(name = "idx_position_stock_code", columnList = "stock_code")
})
@EntityListeners(AuditingEntityListener.class)
public class Position {

    /**
     * The unique identifier for the position.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The securities account to which this position belongs.
     * <p>
     * This field represents a relationship to a {@link SecuritiesAccount} 
     * entity, indicating which account holds this position.
     * </p>
     */
    @Column(name = "securities_account_id", nullable = false)
    private Long securitiesAccountId;

    /**
     * The stock ID associated with this position.
     * <p>
     * This field represents a relationship to a {@link Stock} entity, 
     * indicating the stock that is held within this position.
     * </p>
     */
    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    /**
     * The stock code associated with this position.
     * <p>
     * This field represents a relationship to a {@link Stock} entity, 
     * indicating the stock that is held within this position.
     * </p>
     */
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    /**
     * The number of shares held in this position.
     * <p>
     * This field represents the quantity of stock held within the 
     * given position, indicating how many shares of the stock are 
     * owned in this securities account.
     * </p>
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * The average price at which the shares were acquired.
     * <p>
     * This field represents the average price per share at which the 
     * stock in this position was purchased, recorded as a decimal with 
     * precision for accuracy in financial calculations.
     * </p>
     */
    @Column(name = "average_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal averagePrice;

    /**
     * Flag indicating whether the position is frozen.
     * <p>
     * If this field is true, the position is frozen and cannot be traded.
     * </p>
     */
    @Column(name = "is_frozen", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isFrozen = false;

    /**
     * The number of shares currently frozen in this position.
     * <p>
     * This field represents the quantity of shares currently frozen in the position, 
     * indicating the number of shares that are in trading or pending execution.
     * </p>
     */
    @Column(name = "frozen_quantity", nullable = false)
    private Integer frozenQuantity = 0;
    
    /**
     * The total cost of this position (average price * quantity).
     */
    @Column(name = "total_cost", precision = 20, scale = 2)
    private BigDecimal totalCost;

    /**
     * The timestamp when the position was created.
     * <p>
     * This field automatically records the timestamp when a position is created.
     * </p>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the position was last updated.
     * <p>
     * This field automatically records the timestamp whenever a position is updated.
     * </p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * The list of stop loss and take profit rules associated with this position.
     */
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StopLossTakeProfitRule> stopLossTakeProfitRules = new ArrayList<>();
    
    /**
     * Calculates the available quantity that can be traded or sold.
     * <p>
     * The available quantity is the total quantity of shares held minus the quantity
     * that is currently frozen (e.g., due to pending sell orders). This represents the
     * number of shares that can be immediately sold or used for other transactions.
     * </p>
     * 
     * @return The number of shares available for trading
     */
    @Transient
    public Integer getAvailableQuantity() {
        return quantity - frozenQuantity;
    }
    
    /**
     * Updates the total cost when either quantity or average price changes.
     * <p>
     * This method is automatically called before persisting or updating the position entity.
     * It calculates the total cost by multiplying the quantity by the average price.
     * If either quantity or average price is null, no calculation is performed.
     * </p>
     */
    @PrePersist
    @PreUpdate
    public void updateTotalCost() {
        if (quantity != null && averagePrice != null) {
            this.totalCost = averagePrice.multiply(new BigDecimal(quantity));
        }
    }
    
    /**
     * Calculates the current market value of the position based on the provided current price.
     * <p>
     * This method multiplies the total quantity of shares by the current market price
     * to determine the total value of the position. If either parameter is null,
     * it returns zero to avoid null pointer exceptions.
     * </p>
     * 
     * @param currentPrice The current market price of the stock
     * @return The current market value of the position
     */
    public BigDecimal getCurrentValue(BigDecimal currentPrice) {
        if (currentPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.multiply(new BigDecimal(quantity));
    }
    
    /**
     * Calculates the unrealized profit or loss of the position based on the provided current price.
     * <p>
     * This method determines the difference between the current market value and the
     * cost basis of the position. It calculates this by finding the difference between
     * the current price and the average purchase price, then multiplying by the quantity.
     * If any required parameter is null, it returns zero to avoid null pointer exceptions.
     * </p>
     * 
     * @param currentPrice The current market price of the stock
     * @return The unrealized profit or loss
     */
    public BigDecimal getUnrealizedPnl(BigDecimal currentPrice) {
        if (currentPrice == null || quantity == null || averagePrice == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(averagePrice).multiply(new BigDecimal(quantity));
    }
    
    /**
     * Calculates the unrealized profit or loss percentage.
     * <p>
     * This method determines the percentage gain or loss on the position by comparing
     * the current price with the average purchase price. It uses the formula:
     * ((currentPrice - averagePrice) / averagePrice) * 100.
     * If any required parameter is null or the average price is zero, it returns zero
     * to avoid division by zero errors.
     * </p>
     * 
     * @param currentPrice The current market price of the stock
     * @return The percentage of profit or loss
     */
    public BigDecimal getUnrealizedPnlPercentage(BigDecimal currentPrice) {
        if (averagePrice == null || averagePrice.compareTo(BigDecimal.ZERO) == 0 || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        
        return currentPrice.subtract(averagePrice)
            .divide(averagePrice, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(100));
    }
    
    /**
     * Updates the position quantity and average price after a buy or sell transaction.
     * <p>
     * This method adjusts the position details based on the type of transaction:
     * <ul>
     *   <li>For BUY transactions, it recalculates the average purchase price using a weighted average
     *       and increases the total quantity.</li>
     *   <li>For SELL transactions, it reduces the quantity but maintains the same average price.</li>
     * </ul>
     * After updating the position, it also recalculates the total cost.
     * </p>
     * 
     * @param type The type of transaction (buy/sell)
     * @param quantity The quantity involved in the transaction
     * @param price The price at which the transaction occurred
     */
    public void updatePositionAfterTransaction(OrderType type, int quantity, BigDecimal price) {
        if (OrderType.BUY == type) {
            // Calculate new average price for buy
            BigDecimal oldTotalCost = this.averagePrice.multiply(new BigDecimal(this.quantity));
            BigDecimal newPurchaseCost = price.multiply(new BigDecimal(quantity));
            int newQuantity = this.quantity + quantity;
            
            this.averagePrice = oldTotalCost.add(newPurchaseCost).divide(new BigDecimal(newQuantity), 2, RoundingMode.HALF_UP);
            this.quantity = newQuantity;
        } else if (OrderType.SELL == type) {
            // For selling, we just reduce the quantity but keep the average price
            this.quantity -= quantity;
        }
        
        // Update total cost
        updateTotalCost();
    }
}
