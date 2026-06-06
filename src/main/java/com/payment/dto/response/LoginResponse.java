package com.payment.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UUID userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean twoFactorEnabled;

    private String primaryWalletId;
    private List<WalletInfo> wallets;

    @Data
    @Builder
    public static class WalletInfo {
        private String walletId;
        private String walletName;
        private String currency;
        private boolean isPrimary;
    }
}
