package com.haydenshui.stock.stock;

import com.haydenshui.stock.constants.RocketMQConstants;
import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.stock.StockPriceDTO;
import com.haydenshui.stock.lib.entity.position.StopLossTakeProfitRule;
import com.haydenshui.stock.lib.entity.position.StopLossTakeProfitStatus;
import com.haydenshui.stock.lib.entity.position.StopLossTakeProfitType;
import com.haydenshui.stock.position.PositionService;
import com.haydenshui.stock.trade.order.TradeOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 股票价格变动监听器，用于监听股票价格变化并触发相关操作
 */
@Component
@RocketMQMessageListener(
        consumerGroup = "${rocketmq-config.consumers.stock}",
        topic = "${rocketmq-config.topics.stock-price}",
        selectorType = SelectorType.TAG,
        selectorExpression = "${rocketmq-config.tags.price-update}"
)
public class StockPriceListener implements RocketMQListener<StockPriceDTO> {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceListener.class);

    @Autowired
    private StockService stockService;

    @Autowired
    private StockAlertService alertService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private TradeOrderService tradeOrderService;

    /**
     * 处理价格变动消息
     * 
     * @param priceDTO 价格变动数据
     */
    @Override
    @LogDetails
    public void onMessage(StockPriceDTO priceDTO) {
        try {
            logger.info("收到股票价格变动通知: {}", priceDTO);

            // 1. 处理用户设置的价格提醒
            processStockAlerts(priceDTO);

            // 2. 处理止盈止损规则
            processStopLossTakeProfitRules(priceDTO);

        } catch (Exception e) {
            logger.error("处理股票价格变动消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理股票价格提醒
     */
    private void processStockAlerts(StockPriceDTO priceDTO) {
        try {
            // 检查并触发价格提醒
            List<String> triggeredAlerts = alertService.checkAndTriggerAlerts(
                    priceDTO.getStockCode(), 
                    priceDTO.getCurrentPrice());

            if (!triggeredAlerts.isEmpty()) {
                logger.info("触发了 {} 个股票价格提醒, stockCode={}", 
                        triggeredAlerts.size(), priceDTO.getStockCode());
                
                // 如果需要，这里可以发送通知给用户
                // notificationService.sendPriceAlertNotifications(triggeredAlerts);
            }
        } catch (Exception e) {
            logger.error("处理股票价格提醒失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理止盈止损规则
     */
    private void processStopLossTakeProfitRules(StockPriceDTO priceDTO) {
        try {
            // 1. 获取所有与该股票相关的活跃止盈止损规则
            List<StopLossTakeProfitRule> rules = positionService.getActiveRulesByStockCode(priceDTO.getStockCode());
            
            for (StopLossTakeProfitRule rule : rules) {
                boolean shouldTrigger = false;
                
                // 2. 判断是否满足触发条件
                switch (rule.getType()) {
                    case TAKE_PROFIT:
                        // 当前价格大于等于目标价格时触发止盈
                        shouldTrigger = priceDTO.getCurrentPrice() >= rule.getThreshold();
                        break;
                        
                    case STOP_LOSS:
                        // 当前价格小于等于目标价格时触发止损
                        shouldTrigger = priceDTO.getCurrentPrice() <= rule.getThreshold();
                        break;
                }
                
                // 3. 如果满足条件，触发相应操作
                if (shouldTrigger) {
                    logger.info("触发{}规则, ruleId={}, stockCode={}, price={}, threshold={}", 
                            rule.getType() == StopLossTakeProfitType.TAKE_PROFIT ? "止盈" : "止损",
                            rule.getId(), priceDTO.getStockCode(), 
                            priceDTO.getCurrentPrice(), rule.getThreshold());
                    
                    // 4. 创建自动卖单
                    triggerStopLossTakeProfit(rule, priceDTO);
                    
                    // 5. 更新规则状态
                    rule.setStatus(StopLossTakeProfitStatus.TRIGGERED);
                    rule.setTriggeredPrice(priceDTO.getCurrentPrice());
                    positionService.updateStopLossTakeProfitRule(rule.getId(), rule);
                }
            }
        } catch (Exception e) {
            logger.error("处理止盈止损规则失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 触发止盈止损操作
     */
    private void triggerStopLossTakeProfit(StopLossTakeProfitRule rule, StockPriceDTO priceDTO) {
        try {
            // 创建市价卖单
            tradeOrderService.createStopLossTakeProfitOrder(
                    rule.getSecuritiesAccountId(),
                    rule.getPositionId(),
                    priceDTO.getStockCode(),
                    rule.getQuantity(),
                    priceDTO.getCurrentPrice(),
                    rule.getType());
        } catch (Exception e) {
            logger.error("创建止盈止损订单失败: {}", e.getMessage(), e);
        }
    }
}