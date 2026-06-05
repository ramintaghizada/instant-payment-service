package com.payment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwoFactorSetupResponse {
    private String secret;
    private String qrCodeUrl;
    private String message;
}