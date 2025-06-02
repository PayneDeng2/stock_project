package com.haydenshui.stock.lib.entity.position;

/**
 * 止盈止损规则状态枚举类
 * <p>
 * 该枚举类定义了止盈止损规则的可能状态，包括活跃、已触发、已取消和已过期。
 * 系统使用这些状态来决定是否应该检查并执行相应的止盈止损规则。
 * </p>
 *
 * @author Hzeristo
 * @version 1.0
 * @since 2025-04-20
 */
public enum StopLossTakeProfitStatus {

    /**
     * 活跃状态，表示规则当前有效，系统会检查该规则是否应该触发
     */
    ACTIVE("活跃"),

    /**
     * 已触发状态，表示规则已经满足触发条件并执行了相应的交易操作
     */
    TRIGGERED("已触发"),

    /**
     * 已取消状态，表示规则被用户手动取消
     */
    CANCELED("已取消"),

    /**
     * 已过期状态，表示规则因达到指定的有效期而被系统自动设置为无效
     */
    EXPIRED("已过期");

    /**
     * 规则状态的描述
     */
    private final String description;

    /**
     * 构造函数，初始化规则状态的描述
     * 
     * @param description 规则状态的描述
     */
    StopLossTakeProfitStatus(String description) {
        this.description = description;
    }

    /**
     * 获取规则状态的描述
     * 
     * @return 规则状态的描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据字符串值获取对应的规则状态枚举值
     * <p>
     * 该方法不区分大小写，匹配枚举常量名称。
     * </p>
     * 
     * @param value 表示规则状态的字符串值
     * @return 对应的规则状态枚举值
     * @throws IllegalArgumentException 如果没有找到匹配的规则状态
     */
    public static StopLossTakeProfitStatus fromString(String value) {
        for (StopLossTakeProfitStatus status : StopLossTakeProfitStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid StopLossTakeProfitStatus: " + value);
    }

    /**
     * 根据描述获取对应的规则状态枚举值
     * 
     * @param desc 规则状态的描述
     * @return 对应的规则状态枚举值
     * @throws IllegalArgumentException 如果没有找到匹配的规则状态
     */
    public static StopLossTakeProfitStatus fromDescription(String desc) {
        for (StopLossTakeProfitStatus status : StopLossTakeProfitStatus.values()) {
            if (status.description.equals(desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching StopLossTakeProfitStatus for description: " + desc);
    }

    /**
     * 检查规则状态是否为活跃状态
     * 
     * @return 如果规则状态为活跃则返回true，否则返回false
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 检查规则状态是否为已触发状态
     * 
     * @return 如果规则状态为已触发则返回true，否则返回false
     */
    public boolean isTriggered() {
        return this == TRIGGERED;
    }

    /**
     * 检查规则状态是否为已取消状态
     * 
     * @return 如果规则状态为已取消则返回true，否则返回false
     */
    public boolean isCanceled() {
        return this == CANCELED;
    }

    /**
     * 检查规则状态是否为已过期状态
     * 
     * @return 如果规则状态为已过期则返回true，否则返回false
     */
    public boolean isExpired() {
        return this == EXPIRED;
    }
}