package com.haydenshui.stock.capital;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

=======
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.haydenshui.stock.constants.RocketMQConstants;
<<<<<<< HEAD
import com.haydenshui.stock.lib.dto.CheckDTO;
import com.haydenshui.stock.lib.dto.capital.CapitalAccountTransactionDTO;
import com.haydenshui.stock.lib.entity.account.CapitalAccountType;
import com.haydenshui.stock.lib.msg.TransactionMessage;
=======
import com.haydenshui.stock.lib.dto.capital.CapitalTransactionalDTO;
import com.haydenshui.stock.lib.exception.ResourceInsufficientException;
import com.haydenshui.stock.lib.exception.ResourceNotFoundException;
import com.haydenshui.stock.lib.msg.TransactionMessage;
import com.haydenshui.stock.utils.RocketMQUtils;
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe

/**
 * 资金账户消息监听器，处理与资金相关的消息事件
 */
@Component
@RocketMQMessageListener(topic = RocketMQConstants.TOPIC_CAPITAL, consumerGroup = "capital-update-group")
public class CapitalListener implements RocketMQListener<MessageExt> {

<<<<<<< HEAD
    private final Map<String, Consumer<String>> tagHandlerMap = new HashMap<>();

    public CapitalListener(CapitalAccountService capitalService) {
        tagHandlerMap.put(RocketMQConstants.TAG_CAPITAL_VALIDITY_CHECK, raw -> {
            TransactionMessage<CheckDTO> tmsg = JSON.parseObject(
                raw,
                new TypeReference<TransactionMessage<CheckDTO>>() {}
            );
            capitalService.disableValidity(tmsg.getPayload(), "fund"); 
        });
        tagHandlerMap.put(RocketMQConstants.TAG_CAPITAL_VALIDITY_CONFIRM, raw -> {
        });

        tagHandlerMap.put(RocketMQConstants.TAG_TRADE_CREATE, raw -> {
            TransactionMessage<CapitalAccountTransactionDTO> tmsg = JSON.parseObject(
                raw,
                new TypeReference<TransactionMessage<CapitalAccountTransactionDTO>>() {}
            );
            capitalService.freezeAmount(tmsg.getContext(), tmsg.getPayload(), CapitalAccountType.TRADE);
        });
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        String tag = messageExt.getTags();
        String raw = new String(messageExt.getBody());

        Consumer<String> handler = tagHandlerMap.get(tag);
        if (handler != null) {
            handler.accept(raw);
        } 
=======
    @Autowired
    private CapitalAccountService capitalAccountService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String tag = messageExt.getTags();
        String message = new String(messageExt.getBody());

        TransactionMessage<CapitalTransactionalDTO> msg = JSON.parseObject(message, 
            new TypeReference<TransactionMessage<CapitalTransactionalDTO>>() {});

        BusinessActionContext context = new BusinessActionContext();
        context.setActionName("tradeFreezeCapital");
        context.setXid(msg.getXid());

        CapitalTransactionalDTO dto = msg.getPayload();

        try {
            switch (tag) {
                case RocketMQConstants.TAG_TRADE_CREATE:
                    // 冻结资金
                    capitalAccountService.tradeFreezeCapital(context, dto);
                    break;
                case RocketMQConstants.TAG_TRADE_CONFIRM:
                    // 确认资金变动
                    capitalAccountService.commitTradeFreezeCapital(context, dto);
                    break;
                case RocketMQConstants.TAG_TRADE_CANCEL:
                    // 取消资金冻结
                    capitalAccountService.rollbackTradeFreezeCapital(context, dto);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported tag: " + tag);
            }
        } catch (ResourceInsufficientException e) {
            // 资金不足，发送取消交易消息
            RocketMQUtils.sendMessage("capital", RocketMQConstants.TOPIC_TRADE, RocketMQConstants.TAG_TRADE_CANCEL, context.getXid(), message);
        } catch (ResourceNotFoundException e) {
            // 资源不存在，发送取消交易消息
            RocketMQUtils.sendMessage("capital", RocketMQConstants.TOPIC_TRADE, RocketMQConstants.TAG_TRADE_CANCEL, context.getXid(), message);
        } catch (Exception ex) {
            // 其他异常，发送取消交易消息
            RocketMQUtils.sendMessage("capital", RocketMQConstants.TOPIC_TRADE, RocketMQConstants.TAG_TRADE_CANCEL, context.getXid(), message);
        }
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
    }
}

