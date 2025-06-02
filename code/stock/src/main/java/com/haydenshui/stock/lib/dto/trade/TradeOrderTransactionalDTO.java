package com.haydenshui.stock.lib.dto.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderTransactionalDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long securitiesAccountId;

    private String stockCode;

    private String orderType;

    private Integer orderQuantity;

    private BigDecimal orderPrice;

    private String orderValidity;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
}
