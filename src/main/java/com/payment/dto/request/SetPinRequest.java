package com.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SetPinRequest {
    @NotBlank
    private String walletId;
    
    @NotBlank
    @Pattern(regexp = "\\d{4,6}", message = "PIN must be 4-6 digits")
    private String pin;
}