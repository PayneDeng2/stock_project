package com.haydenshui.stock.lib.entity.trade;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Represents a trade execution in the trading system.
 *
 * <p>This entity captures the details of an execution for a specific trade order, 
 * including the related accounts, executed price, executed quantity, and the time of execution.</p>
 * 
 * @author Hzer
 * @version 1.0
 * @since 2025-03-04
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trade_execution", indexes = {
    @Index(name = "idx_trade_execution_trade_order_id", columnList = "trade_order_id"),
    @Index(name = "idx_trade_execution_securities_account_id", columnList = "securities_account_id"),
    @Index(name = "idx_trade_execution_capital_account_id", columnList = "capital_account_id")
})
@EntityListeners(AuditingEntityListener.class)
public class TradeExecution {

    /**
     * The unique identifier for the trade execution.
     * This is the primary key for the TradeExecution entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The trade order associated with this trade execution.
     * This represents the original order that was executed.
     */
    @Column(name = "trade_order_id", nullable = false)
    private Long tradeOrderId;

    /**
     * The securities account associated with the trade execution.
     * Represents the account holding the securities involved in the trade.
     */
    @Column(name = "securities_account_id", nullable = false)
    private Long securitiesAccountId;

    /**
     * The capital account associated with the trade execution.
     * Represents the account involved in the financial transaction of the trade.
     */
    @Column(name = "capital_account_id", nullable = false)
    private Long capitalAccountId;

    /**
     * The price at which the trade was executed.
     * The value is stored with a precision of 18 and scale of 2 to ensure accurate representation of financial data.
     */
    @Column(name = "executed_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal executedPrice;

    /**
     * The quantity of securities executed in this trade.
     * This represents the number of units involved in the trade execution.
     */
    @Column(name = "executed_quantity", nullable = false)
    private Integer executedQuantity;

    /**
     * The timestamp when the trade was executed.
     * This field captures the exact time of the trade execution.
     */
    @CreatedDate
    @Column(name = "execution_time", nullable = false)
    private LocalDateTime executionTime;

    /**
     * The stock code involved in this trade execution.
     */
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

}
