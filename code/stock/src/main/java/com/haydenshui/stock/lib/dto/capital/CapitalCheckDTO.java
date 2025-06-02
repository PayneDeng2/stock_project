package com.haydenshui.stock.lib.dto.capital;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CapitalCheckDTO {

    private Long capitalAccountId;

    private Long securitiesAccountId;

    private String type;

    private boolean passed;

    private String reason;
    
}
