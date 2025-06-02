package com.haydenshui.stock.lib.entity.stock;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a stock price at a specific point in time.
 * <p>
 * This class is used to track historical stock prices, providing a record of how a stock's
 * price has changed over time. Each instance represents a price data point for a specific stock
 * at a specific time.
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
@Table(name = "stock_price", indexes = {
        @Index(name = "idx_price_stock_id", columnList = "stock_id"),
        @Index(name = "idx_price_stock_code", columnList = "stock_code"),
        @Index(name = "idx_price_time", columnList = "time")
})
public class StockPrice {

    /**
     * Unique identifier for the stock price record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ID of the stock this price relates to.
     */
    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    /**
     * The code of the stock this price relates to.
     */
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    /**
     * The price of the stock.
     */
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    /**
     * The trading volume at this price point, if available.
     */
    @Column(name = "volume")
    private Long volume;

    /**
     * The time at which this price was recorded.
     */
    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    /**
     * The timestamp when this record was created in the database.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Calculates the price change from a previous price.
     * <p>
     * This method computes the absolute difference between the current price and a provided
     * previous price. If the previous price is null, it returns zero to avoid null pointer exceptions.
     * </p>
     * 
     * @param previousPrice The previous price to compare with
     * @return The difference between this price and the previous price
     */
    @Transient
    public BigDecimal calculatePriceChange(BigDecimal previousPrice) {
        if (previousPrice == null) {
            return BigDecimal.ZERO;
        }
        return price.subtract(previousPrice);
    }

    /**
     * Calculates the percentage change from a previous price.
     * <p>
     * This method computes the percentage difference between the current price and a provided
     * previous price. The formula used is ((current - previous) / previous) * 100, which gives the
     * percentage change.
     * </p>
     * <p>
     * If the previous price is null or zero, it returns zero to avoid division by zero errors.
     * </p>
     * 
     * @param previousPrice The previous price to compare with
     * @return The percentage change between this price and the previous price
     */
    @Transient
    public BigDecimal calculatePercentageChange(BigDecimal previousPrice) {
        if (previousPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return price.subtract(previousPrice)
                .divide(previousPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }
}