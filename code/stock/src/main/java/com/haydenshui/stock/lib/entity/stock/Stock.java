package com.haydenshui.stock.lib.entity.stock;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a stock listed on a securities exchange.
 *
 * <p>This entity contains information about a stock, including its stock code, name, company, exchange, and other relevant details.
 * It is used to model a stock within the system for tracking and analysis purposes.</p>
 * 
 * @author Hzeristo
 * @version 1.0
 * @since 2025-03-04
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock", indexes = {
    @Index(name = "idx_stock_code", columnList = "stock_code"),
    @Index(name = "idx_exchange", columnList = "exchange")
})
public class Stock {

    /**
     * The unique identifier for the stock.
     * This is automatically generated as the primary key for the stock entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The stock's unique code.
     * This is used to identify the stock on the exchange and is indexed for quick search.
     */
    @Column(name = "stock_code", unique = true, nullable = false)
    private String stockCode;

    /**
     * The name of the stock.
     * This is the name by which the stock is commonly known.
     */
    @Column(name = "stock_name", nullable = false)
    private String stockName;

    /**
     * The name of the company that issues the stock.
     * This is the company that the stock represents, typically its full legal name.
     */
    @Column(name = "company_name", nullable = false)
    private String companyName;

    /**
     * The exchange where the stock is listed.
     * Examples: SSE (Shanghai Stock Exchange), SZSE (Shenzhen Stock Exchange).
     */
    @Column(name = "exchange", nullable = false, length = 10)
    private String exchange;

    /**
     * The industry sector to which the company belongs.
     * This helps in classifying the stock based on the type of business the company is involved in.
     */
    @Column(name = "industry")
    private String industry;

    /**
     * The sector within the industry to which the company belongs.
     * This can help further categorize the company based on its specialization.
     */
    @Column(name = "sector")
    private String sector;

    /**
     * The date the stock was listed on the exchange.
     * This marks the official start date of the stock's trading activity.
     */
    @Column(name = "listing_date")
    private LocalDate listingDate;

    /**
     * The International Securities Identification Number (ISIN) of the stock.
     * This is a unique identifier for the stock used internationally, often required for global trading.
     */
    @Column(name = "isin", unique = true, length = 20)
    private String isin;

    /**
     * The status of the stock.
     * This field tracks the current state of the stock, indicating whether it is ACTIVE, DELISTED, or SUSPENDED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StockStatus status;
    
    /**
     * The current price of the stock.
     * This is the most recent price at which the stock was traded.
     */
    @Column(name = "current_price", precision = 18, scale = 2)
    private BigDecimal currentPrice;
    
    /**
     * The opening price of the stock for the current trading day.
     */
    @Column(name = "open_price", precision = 18, scale = 2)
    private BigDecimal openPrice;
    
    /**
     * The closing price of the stock from the previous trading day.
     */
    @Column(name = "close_price", precision = 18, scale = 2)
    private BigDecimal closePrice;
    
    /**
     * The highest price of the stock during the current trading day.
     */
    @Column(name = "high_price", precision = 18, scale = 2)
    private BigDecimal highPrice;
    
    /**
     * The lowest price of the stock during the current trading day.
     */
    @Column(name = "low_price", precision = 18, scale = 2)
    private BigDecimal lowPrice;
    
    /**
     * The closing price of the stock from the previous trading day.
     * This is used to calculate price change percentage.
     */
    @Column(name = "previous_close_price", precision = 18, scale = 2)
    private BigDecimal previousClosePrice;
    
    /**
     * The total number of shares traded during the current trading day.
     */
    @Column(name = "volume")
    private Long volume;
    
    /**
     * The total value of shares traded during the current trading day.
     */
    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;
    
    /**
     * Indicates if this stock is actually an index (like SSE Composite Index)
     */
    @Column(name = "is_index")
    private Boolean isIndex = false;
    
    /**
     * The last time the stock price was updated.
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    /**
     * Calculates the price change percentage from previous close.
     * 
     * @return The percentage change in price compared to the previous close, or null if previousClosePrice is null or zero.
     */
    @Transient
    public BigDecimal getPriceChangePercentage() {
        if (previousClosePrice == null || previousClosePrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        if (currentPrice == null) {
            return BigDecimal.ZERO;
        }
        
        return currentPrice.subtract(previousClosePrice)
                .divide(previousClosePrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }

}
