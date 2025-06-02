package com.haydenshui.stock.lib.entity.position;

/**
 * 止盈止损规则类型枚举类
 * <p>
 * 该枚举类定义了止盈止损规则的类型，包括止损和止盈两种基础类型。
 * 系统根据规则类型决定触发条件的计算方式和执行相应的交易操作。
 * </p>
 *
 * @author Hzeristo
 * @version 1.0
 * @since 2025-04-20
 */
public enum StopLossTakeProfitType {

    /**
     * 止损类型，在股票价格低于设定阈值时触发卖出操作，以限制亏损
     */
    STOP_LOSS("止损"),

    /**
     * 止盈类型，在股票价格高于设定阈值时触发卖出操作，以锁定收益
     */
    TAKE_PROFIT("止盈");

    /**
     * 规则类型的描述
     */
    private final String description;

    /**
     * 构造函数，初始化规则类型的描述
     * 
     * @param description 规则类型的描述
     */
    StopLossTakeProfitType(String description) {
        this.description = description;
    }

    /**
     * 获取规则类型的描述
     * 
     * @return 规则类型的描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据字符串值获取对应的规则类型枚举值
     * <p>
     * 该方法不区分大小写，匹配枚举常量名称。
     * </p>
     * 
     * @param value 表示规则类型的字符串值
     * @return 对应的规则类型枚举值
     * @throws IllegalArgumentException 如果没有找到匹配的规则类型
     */
    public static StopLossTakeProfitType fromString(String value) {
        for (StopLossTakeProfitType type : StopLossTakeProfitType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid StopLossTakeProfitType: " + value);
    }

    /**
     * 根据描述获取对应的规则类型枚举值
     * 
     * @param desc 规则类型的描述
     * @return 对应的规则类型枚举值
     * @throws IllegalArgumentException 如果没有找到匹配的规则类型
     */
    public static StopLossTakeProfitType fromDescription(String desc) {
        for (StopLossTakeProfitType type : StopLossTakeProfitType.values()) {
            if (type.description.equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching StopLossTakeProfitType for description: " + desc);
    }

    /**
     * 检查规则类型是否为止损类型
     * 
     * @return 如果规则类型为止损则返回true，否则返回false
     */
    public boolean isStopLoss() {
        return this == STOP_LOSS;
    }

    /**
     * 检查规则类型是否为止盈类型
     * 
     * @return 如果规则类型为止盈则返回true，否则返回false
     */
    public boolean isTakeProfit() {
        return this == TAKE_PROFIT;
    }
}