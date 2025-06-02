package com.haydenshui.stock.trade.order;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.haydenshui.stock.constants.RocketMQConstants;
import com.haydenshui.stock.lib.dto.trade.TradeOrderDTO;
import com.haydenshui.stock.lib.entity.trade.OrderStatus;
import com.haydenshui.stock.lib.msg.TransactionMessage;
import com.haydenshui.stock.utils.RocketMQUtils;

/**
 * 交易订单消息监听器，处理与订单相关的消息事件
 */
@Component
@RocketMQMessageListener(topic = RocketMQConstants.TOPIC_ORDER, consumerGroup = "order-processing-group")
public class TradeOrderListener implements RocketMQListener<MessageExt> {

    @Autowired
    private TradeOrderService orderService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String tag = messageExt.getTags();
        String message = new String(messageExt.getBody());
        
        try {
            TransactionMessage<TradeOrderDTO> msg = JSON.parseObject(message, 
                new TypeReference<TransactionMessage<TradeOrderDTO>>() {});
            
            String xid = msg.getXid();
            TradeOrderDTO dto = msg.getPayload();
            
            BusinessActionContext context = new BusinessActionContext();
            context.setActionName("placeTradeOrder");
            context.setXid(xid);
            
            switch (tag) {
                case RocketMQConstants.TAG_ORDER_CREATE:
                    // 创建订单
                    boolean prepared = orderService.placeTradeOrder(context, dto);
                    if (prepared) {
                        // 冻结资金
                        notifyCapitalFreeze(xid, dto);
                    }
                    break;
                    
                case RocketMQConstants.TAG_ORDER_MATCH:
                    // 订单匹配成功
                    orderService.commitPlaceTradeOrder(context);
                    // 交易执行
                    notifyExecution(xid, dto);
                    break;
                    
                case RocketMQConstants.TAG_ORDER_CANCEL:
                    // 订单取消
                    orderService.rollbackPlaceTradeOrder(context);
                    break;
                    
                case RocketMQConstants.TAG_ORDER_UPDATE:
                    // 订单状态更新
                    orderService.updateOrder(dto);
                    
                    // 如果订单状态变为已完成，通知相关模块
                    if (OrderStatus.COMPLETED.name().equals(dto.getStatus())) {
                        notifyOrderCompleted(xid, dto);
                    }
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported tag: " + tag);
            }
        } catch (Exception e) {
            // 异常处理
            System.err.println("处理订单消息失败: " + e.getMessage());
            e.printStackTrace();
            
            // 如果是创建订单的消息，需要发送取消消息
            if (RocketMQConstants.TAG_ORDER_CREATE.equals(tag)) {
                try {
                    TransactionMessage<TradeOrderDTO> msg = JSON.parseObject(message, 
                        new TypeReference<TransactionMessage<TradeOrderDTO>>() {});
                    
                    // 通知资金系统回滚
                    RocketMQUtils.sendMessage("trade-order", 
                        RocketMQConstants.TOPIC_CAPITAL, 
                        RocketMQConstants.TAG_TRADE_CANCEL, 
                        msg.getXid(), 
                        msg.getPayload());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 通知资金系统冻结资金
     */
    private void notifyCapitalFreeze(String xid, TradeOrderDTO dto) {
        RocketMQUtils.sendMessage("trade-order", 
            RocketMQConstants.TOPIC_CAPITAL, 
            RocketMQConstants.TAG_TRADE_CREATE, 
            xid, dto);
    }
    
    /**
     * 通知交易执行系统
     */
    private void notifyExecution(String xid, TradeOrderDTO dto) {
        RocketMQUtils.sendMessage("trade-order", 
            RocketMQConstants.TOPIC_EXECUTION, 
            RocketMQConstants.TAG_EXECUTION_CREATE, 
            xid, dto);
    }
    
    /**
     * 通知订单完成
     */
    private void notifyOrderCompleted(String xid, TradeOrderDTO dto) {
        // 可以通知其他系统，如通知用户订单完成等
    }
}