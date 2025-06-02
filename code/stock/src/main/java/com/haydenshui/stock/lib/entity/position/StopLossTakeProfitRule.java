package com.haydenshui.stock.lib.entity.position;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 止盈止损规则实体类
 * <p>
 * 该实体类用于存储用户对特定持仓设置的止盈止损规则，包含触发价格、规则类型等信息。
 * 当股票价格达到设定的条件时，系统将自动创建交易订单执行相应操作。
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
@Table(name = "stop_loss_take_profit_rule", indexes = {
        @Index(name = "idx_sltp_position_id", columnList = "position_id"),
        @Index(name = "idx_sltp_securities_account_id", columnList = "securities_account_id"),
        @Index(name = "idx_sltp_stock_code", columnList = "stock_code")
})
public class StopLossTakeProfitRule {

    /**
     * 规则ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的持仓
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;
    
    /**
     * 关联的证券账户ID
     */
    @Column(name = "securities_account_id", nullable = false)
    private Long securitiesAccountId;
    
    /**
     * 股票代码
     */
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    /**
     * 规则类型：止盈或止损
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private StopLossTakeProfitType type;

    /**
     * 触发价格阈值
     */
    @Column(name = "threshold", nullable = false, precision = 18, scale = 2)
    private BigDecimal threshold;

    /**
     * 卖出数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 规则状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StopLossTakeProfitStatus status = StopLossTakeProfitStatus.ACTIVE;

    /**
     * 触发价格
     */
    @Column(name = "triggered_price", precision = 18, scale = 2)
    private BigDecimal triggeredPrice;

    /**
     * 规则创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 规则更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 规则触发时间
     */
    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    /**
     * 检查给定价格是否满足触发条件
     * <p>
     * 此方法根据规则类型（止盈或止损）和给定的当前价格，判断是否应该触发规则执行。
     * 如果规则不处于活跃状态，则不会被触发。对于止损规则，当前价格低于阈值时触发；
     * 对于止盈规则，当前价格高于阈值时触发。
     * </p>
     *
     * @param currentPrice 当前股票价格
     * @return 如果满足触发条件则返回true，否则返回false
     */
    public boolean shouldTrigger(BigDecimal currentPrice) {
        if (status != StopLossTakeProfitStatus.ACTIVE) {
            return false;
        }

        if (type == StopLossTakeProfitType.STOP_LOSS) {
            // 止损：当前价格低于阈值时触发
            return currentPrice.compareTo(threshold) <= 0;
        } else {
            // 止盈：当前价格高于阈值时触发
            return currentPrice.compareTo(threshold) >= 0;
        }
    }

    /**
     * 标记规则已触发
     * <p>
     * 当规则被触发时，此方法更新规则状态为已触发，并记录触发时间和触发时的价格。
     * 这有助于后续分析和审计该规则的执行情况。
     * </p>
     * 
     * @param triggerPrice 触发时的股票价格
     */
    public void markAsTriggered(BigDecimal triggerPrice) {
        this.status = StopLossTakeProfitStatus.TRIGGERED;
        this.triggeredAt = LocalDateTime.now();
        this.triggeredPrice = triggerPrice;
    }
}