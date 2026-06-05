
package com.payment.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionStatus {
    private String transactionId;
    private String status;
    private String fromWallet;
    private String toWallet;
    private BigDecimal amount;
    private String message;
    private LocalDateTime timestamp;
    private Integer retryCount;
}