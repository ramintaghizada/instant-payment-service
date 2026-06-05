package com.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorRequest {
    @NotBlank
    private String code;
    
    private boolean enable;
}