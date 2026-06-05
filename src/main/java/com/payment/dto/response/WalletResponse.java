package com.payment.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class WalletResponse {
    private String walletId;
    private String walletName;
    private BigDecimal balance;
    private String currency;
    private String status;
    private boolean isPrimary;
}