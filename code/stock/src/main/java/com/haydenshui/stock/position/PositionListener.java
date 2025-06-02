package com.haydenshui.stock.position;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.haydenshui.stock.constants.ErrorCode;
import com.haydenshui.stock.constants.RocketMQConstants;
import com.haydenshui.stock.lib.dto.position.PositionTransactionalDTO;
import com.haydenshui.stock.lib.exception.ResourceInsufficientException;
import com.haydenshui.stock.lib.exception.ResourceNotFoundException;
import com.haydenshui.stock.lib.msg.TransactionMessage;
import com.haydenshui.stock.utils.RocketMQUtils;

import java.util.List;

@Component
@RocketMQMessageListener(topic = RocketMQConstants.TOPIC_POSITION, consumerGroup = RocketMQConstants.CONSUMER_POSITION)
public class PositionListener implements RocketMQListener<MessageExt> {

    private final Map<String, Consumer<TransactionMessage<PositionTransactionalDTO>>> tagHandlerMap;

    public PositionListener(PositionService positionService) {
        this.tagHandlerMap = new HashMap<>();
        tagHandlerMap.put(RocketMQConstants.TAG_TRADE_CREATE, tmsg ->
            positionService.tradeUpdatePosition(tmsg.getContext(), tmsg.getPayload())
        );
        tagHandlerMap.put(RocketMQConstants.TAG_TRADE_CONFIRM, tmsg -> {
            if(positionService.commitTradeUpdatePosition(tmsg.getContext(), tmsg.getPayload())) {
                RocketMQUtils.sendMessage(
                    "trade",
                    RocketMQConstants.TOPIC_TRADE,
                    RocketMQConstants.TAG_TRADE_CONFIRM,
                    tmsg.getMsgXid(),
                    JSON.toJSONString(tmsg)
                );    
            }
        });
        tagHandlerMap.put(RocketMQConstants.TAG_TRADE_CANCEL, tmsg -> {
            positionService.rollbackTradeUpdatePosition(tmsg.getContext(), tmsg.getPayload());
        });
    }

    @Autowired
    private StopLossTakeProfitRuleRepository stopLossTakeProfitRuleRepository;

    @Override
    public void onMessage(MessageExt messageExt) {
        String tag = messageExt.getTags();
<<<<<<< HEAD
        String rawMessage = new String(messageExt.getBody());
    
        TransactionMessage<PositionTransactionalDTO> tmsg = JSON.parseObject(
            rawMessage,
            new TypeReference<>() {}
        );
    
        Consumer<TransactionMessage<PositionTransactionalDTO>> handler = tagHandlerMap.get(tag);
        if (handler == null) 
            throw new IllegalArgumentException("Unsupported tag: " + tag);
=======
        String message = new String(messageExt.getBody());

        TransactionMessage<PositionTransactionalDTO> msg = JSON.parseObject(message, 
            new TypeReference<TransactionMessage<PositionTransactionalDTO>>() {});

        BusinessActionContext context = new BusinessActionContext();
        context.setActionName("tradeUpdatePosition");
        context.setXid(msg.getXid());

        PositionTransactionalDTO dto = msg.getPayload();
        List<StopLossTakeProfitRule> rules = stopLossTakeProfitRuleRepository.findByPositionId(dto.getId());

        for (StopLossTakeProfitRule rule : rules) {
            if (rule.getStatus() == RuleStatus.ACTIVE) {
                if ((rule.getType() == RuleType.STOP_LOSS && dto.getTransactionalPrice().compareTo(rule.getThreshold()) <= 0) ||
                    (rule.getType() == RuleType.TAKE_PROFIT && dto.getTransactionalPrice().compareTo(rule.getThreshold()) >= 0)) {
                    rule.setStatus(RuleStatus.TRIGGERED);
                    stopLossTakeProfitRuleRepository.save(rule);
                    // TODO: 触发止盈止损操作
                }
            }
        }

>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
        try {
            handler.accept(tmsg);
        } catch (Exception e) {
            String errorCode = e instanceof ResourceInsufficientException ? ErrorCode.RESOURCE_INSUFFICIENT.getCode()
                               : e instanceof ResourceNotFoundException ? ErrorCode.RESOURCE_NOT_FOUND.getCode()
                               : "UNKNOWN_ERROR";
    
            tmsg.setErrorCode(errorCode);
            tmsg.setErrorMessage(e.getMessage());
    
            RocketMQUtils.sendMessage(
                "position",
                RocketMQConstants.TOPIC_POSITION,
                RocketMQConstants.TAG_TRADE_CANCEL,
                tmsg.getMsgXid(),
                JSON.toJSONString(tmsg)
            );
        }
    }
    
}

