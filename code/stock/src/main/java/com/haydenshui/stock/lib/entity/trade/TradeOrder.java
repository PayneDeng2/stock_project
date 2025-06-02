package com.haydenshui.stock.lib.entity.trade;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity class representing a trade order in the system.
 *
 * <p>This class is used to map a trade order record in the database, 
 * capturing details about the trade such as the securities account, 
 * stock code, order type, quantity, price, validity, kind, status, 
 * and the time of the order.</p>
 * 
 * @author Hzer
 * @version 1.0
 * @since 2025-03-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trade_order", indexes = {
    @Index(name = "idx_securities_account_id", columnList = "securities_account_id"),
    @Index(name = "idx_stock_code", columnList = "stock_code"),
})
@EntityListeners(AuditingEntityListener.class)
public class TradeOrder {

    /**
     * The unique identifier for this trade order.
     * This is automatically generated as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The securities account associated with this trade order.
     * This represents the account that placed this trade order.
     */
    @Column(name = "securities_account_id", nullable = false)
    private Long securitiesAccountId;
    
    /**
     * The capital account associated with this trade order.
     * This represents the account that will be used for the financial transaction.
     */
    @Column(name = "capital_account_id", nullable = false)
    private Long capitalAccountId;
    
    /**
     * The stock code of the stock being traded.
     * This uniquely identifies the stock involved in this trade.
     */
    @Column(name = "stock_code", nullable = false)
    private String stockCode;
    
    /**
     * The type of the order (buy or sell).
     * This indicates whether the order is to buy or sell the stock.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    /**
     * The quantity of the stock to be traded in this order.
     * This indicates the number of units of the stock the user wishes to buy or sell.
     */
    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    /**
     * The price at which the stock should be bought or sold in this order.
     * This is the price per unit of the stock.
     */
    @Column(name = "order_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal orderPrice;

    /**
     * The validity of the order, specifying how long the order will remain active.
     * For example, whether the order is valid only for the day or for a longer period.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_validity", nullable = false)
    private OrderValidity orderValidity;

    /**
     * The kind of the order (e.g., market or limit).
     * This defines whether the order is a market order, limit order, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_kind", nullable = false)
    private OrderKind orderKind;

    /**
     * The status of the order (e.g., pending, executed, canceled).
     * This indicates the current state of the order.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;
    
    /**
     * The timestamp when the order was placed.
     * This records the exact time when the order was created.
     */
    @CreatedDate
    @Column(name = "order_time", nullable = false)

    private LocalDateTime orderTime;

    /**
     * The timestamp when the order was executed or completed.
     * This records the exact time when the order was filled or completed.
     */
    @LastModifiedDate
    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

}
