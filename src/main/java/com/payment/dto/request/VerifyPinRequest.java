package com.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPinRequest {
    @NotBlank
    private String walletId;
    
    @NotBlank
    private String pin;
}