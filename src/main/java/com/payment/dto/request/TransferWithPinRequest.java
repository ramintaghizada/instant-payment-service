package com.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferWithPinRequest {
    @NotBlank
    private String fromWalletId;
    
    @NotBlank
    private String toWalletId;
    
    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotBlank
    private String pin;
    
    @NotBlank
    private String idempotencyKey;
}