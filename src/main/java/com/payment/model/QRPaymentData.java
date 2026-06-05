
package com.payment.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QRPaymentData {
    private String merchantWalletId;
    private String merchantName;
    private BigDecimal amount;
    private String description;
    private String terminalId;
    private Long timestamp;
}