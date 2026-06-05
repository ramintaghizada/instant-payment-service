
package com.payment.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private boolean success;
    private String message;
    private UUID userId;
    private String walletId;
    private boolean requiresTwoFactor;
}
