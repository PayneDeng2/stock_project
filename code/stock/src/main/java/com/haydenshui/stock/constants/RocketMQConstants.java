package com.haydenshui.stock.constants;

/**
 * RocketMQ 常量类，定义所有主题和标签名称
 */
public class RocketMQConstants {

    // 生产者名称
    public static final String PRODUCER_TRADE = "${rocketmq-config.producers.trade}";
    public static final String PRODUCER_CAPITAL = "${rocketmq-config.producers.capital}";
    public static final String PRODUCER_POSITION = "${rocketmq-config.producers.position}";
<<<<<<< HEAD
    public static final String PRODUCER_SECURITIES = "${rocketmq-config.producers.securities}";

    public static final String CONSUMER_TRADE = "${rocketmq-config.consumers.trade}";
    public static final String CONSUMER_CAPITAL = "${rocketmq-config.consumers.capital}";
    public static final String CONSUMER_POSITION = "${rocketmq-config.consumers.position}";
    public static final String CONSUMER_SECURITIES = "${rocketmq-config.consumers.securities}";
=======
    public static final String PRODUCER_STOCK = "${rocketmq-config.producers.stock}";
    public static final String PRODUCER_EXECUTION = "${rocketmq-config.producers.execution}";
    
    // 消费者组名称
    public static final String CONSUMER_TRADE = "${rocketmq-config.consumers.trade}";
    public static final String CONSUMER_CAPITAL = "${rocketmq-config.consumers.capital}";
    public static final String CONSUMER_POSITION = "${rocketmq-config.consumers.position}";
    public static final String CONSUMER_STOCK = "${rocketmq-config.consumers.stock}";
    public static final String CONSUMER_EXECUTION = "${rocketmq-config.consumers.execution}";
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe

    // 主题名称
    public static final String TOPIC_TRADE = "${rocketmq-config.topics.trade}";
    public static final String TOPIC_MATCH = "${rocketmq-config.topics.match}";
    public static final String TOPIC_POSITION = "${rocketmq-config.topics.position}";
    public static final String TOPIC_CAPITAL = "${rocketmq-config.topics.capital}";
<<<<<<< HEAD
    public static final String TOPIC_SECURITIES = "${rocketmq-config.topics.securities}";
=======
    public static final String TOPIC_ORDER = "${rocketmq-config.topics.order}";
    public static final String TOPIC_EXECUTION = "${rocketmq-config.topics.execution}";
    public static final String TOPIC_STOCK_PRICE = "${rocketmq-config.topics.stock-price}";
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe

    // 交易相关标签
    public static final String TAG_TRADE_CREATE = "${rocketmq-config.tags.trade-create}";
    public static final String TAG_TRADE_CONFIRM = "${rocketmq-config.tags.trade-confirm}";
    public static final String TAG_TRADE_CANCEL = "${rocketmq-config.tags.trade-cancel}";
<<<<<<< HEAD
    public static final String TAG_TRADE_VALIDITY_CHECK = "${rocketmq-config.tags.trade-validity-check}";
    public static final String TAG_CAPITAL_CHECK = "${rocketmq-config.tags.capital-check}";
    public static final String TAG_CAPITAL_VALIDITY_CHECK = "${rocketmq-config.tags.capital_validity-check}";
    public static final String TAG_CAPITAL_VALIDITY_CONFIRM = "${rocketmq-config.tags.capital_validity-confirm}";
    public static final String TAG_SECURITIES_CHECK = "${rocketmq-config.tags.securities-check}";
=======
    
    // 资金相关标签
    public static final String TAG_CAPITAL_CHECK = "${rocketmq-config.tags.capital-check}";
    public static final String TAG_CAPITAL_FREEZE = "${rocketmq-config.tags.capital-freeze}";
    public static final String TAG_CAPITAL_UNFREEZE = "${rocketmq-config.tags.capital-unfreeze}";
    public static final String TAG_CAPITAL_DEDUCT = "${rocketmq-config.tags.capital-deduct}";
    
    // 订单相关标签
    public static final String TAG_ORDER_CREATE = "${rocketmq-config.tags.order-create}";
    public static final String TAG_ORDER_MATCH = "${rocketmq-config.tags.order-match}";
    public static final String TAG_ORDER_CANCEL = "${rocketmq-config.tags.order-cancel}";
    public static final String TAG_ORDER_UPDATE = "${rocketmq-config.tags.order-update}";
    
    // 执行相关标签
    public static final String TAG_EXECUTION_CREATE = "${rocketmq-config.tags.execution-create}";
    public static final String TAG_EXECUTION_UPDATE = "${rocketmq-config.tags.execution-update}";
    public static final String TAG_EXECUTION_CANCEL = "${rocketmq-config.tags.execution-cancel}";
    
    // 股票价格相关标签
    public static final String TAG_PRICE_UPDATE = "${rocketmq-config.tags.price-update}";
    public static final String TAG_MARKET_OPEN = "${rocketmq-config.tags.market-open}";
    public static final String TAG_MARKET_CLOSE = "${rocketmq-config.tags.market-close}";
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe

    private RocketMQConstants() {} // No instantiation
}