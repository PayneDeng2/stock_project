package com.haydenshui.stock.trade.execution;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.haydenshui.stock.constants.RocketMQConstants;
import com.haydenshui.stock.lib.dto.trade.TradeExecutionDTO;
import com.haydenshui.stock.lib.msg.TransactionMessage;
import com.haydenshui.stock.utils.RocketMQUtils;

/**
 * 交易执行消息监听器，处理与交易执行相关的消息事件
 */
@Component
@RocketMQMessageListener(topic = RocketMQConstants.TOPIC_EXECUTION, consumerGroup = "trade-execution-group")
public class TradeExecutionListener implements RocketMQListener<MessageExt> {

    @Autowired
    private TradeExecutionService executionService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String tag = messageExt.getTags();
        String message = new String(messageExt.getBody());
        
        try {
            TransactionMessage<TradeExecutionDTO> msg = JSON.parseObject(message, 
                new TypeReference<TransactionMessage<TradeExecutionDTO>>() {});
            
            TradeExecutionDTO dto = msg.getPayload();
            
            switch (tag) {
                case RocketMQConstants.TAG_EXECUTION_CREATE:
                    // 创建交易执行记录
                    executionService.createExecution(dto);
                    // 发送消息通知持仓和资金更新
                    notifyPositionAndCapital(msg.getXid(), dto, RocketMQConstants.TAG_TRADE_CONFIRM);
                    break;
                    
                case RocketMQConstants.TAG_EXECUTION_UPDATE:
                    // 更新交易执行记录
                    executionService.updateExecution(dto);
                    break;
                    
                case RocketMQConstants.TAG_EXECUTION_CANCEL:
                    // 取消交易执行
                    executionService.cancelExecution(dto);
                    // 发送消息通知持仓和资金回滚
                    notifyPositionAndCapital(msg.getXid(), dto, RocketMQConstants.TAG_TRADE_CANCEL);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported tag: " + tag);
            }
        } catch (Exception e) {
            // 异常处理
            System.err.println("处理交易执行消息失败: " + e.getMessage());
            e.printStackTrace();
            // 如果是创建交易执行的消息，则需要发送取消消息
            if (RocketMQConstants.TAG_EXECUTION_CREATE.equals(tag)) {
                try {
                    TransactionMessage<TradeExecutionDTO> msg = JSON.parseObject(message, 
                        new TypeReference<TransactionMessage<TradeExecutionDTO>>() {});
                    notifyPositionAndCapital(msg.getXid(), msg.getPayload(), RocketMQConstants.TAG_TRADE_CANCEL);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 通知持仓和资金系统进行相应处理
     * 
     * @param xid 事务ID
     * @param dto 交易执行DTO
     * @param tag 消息标签
     */
    private void notifyPositionAndCapital(String xid, TradeExecutionDTO dto, String tag) {
        // 通知持仓系统
        RocketMQUtils.sendMessage("trade-execution", RocketMQConstants.TOPIC_POSITION, tag, xid, dto);
        
        // 通知资金系统
        RocketMQUtils.sendMessage("trade-execution", RocketMQConstants.TOPIC_CAPITAL, tag, xid, dto);
    }
}