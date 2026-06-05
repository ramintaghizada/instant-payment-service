package com.payment.service;

import com.payment.model.Wallet;
import com.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSecurityService {

    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder pinEncoder = new BCryptPasswordEncoder(8);

    @Transactional
    public void setWalletPin(UUID userId, String walletId, String pin) {
        log.info("Setting PIN for wallet: {} and user: {}", walletId, userId);

        Wallet wallet = walletRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Wallet not found for ID: " + walletId + " and user: " + userId));

        validatePinStrength(pin);

        String salt = generatePinSalt();
        String pinHash = pinEncoder.encode(pin + salt);

        wallet.setPinHash(pinHash);
        wallet.setPinSalt(salt);
        walletRepository.save(wallet);

        log.info("PIN set successfully for wallet: {}", walletId);
    }

    public boolean verifyWalletPin(String walletId, String pin) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));

        if (wallet.getPinHash() == null) {
            throw new RuntimeException("PIN not set for this wallet");
        }

        boolean isValid = pinEncoder.matches(pin + wallet.getPinSalt(), wallet.getPinHash());

        if (!isValid) {
            throw new RuntimeException("Invalid PIN");
        }

        return true;
    }

    private void validatePinStrength(String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new RuntimeException("PIN must be 4-6 digits");
        }

        if (pin.equals("1234") || pin.equals("0000") || pin.equals("1111")) {
            throw new RuntimeException("PIN is too weak. Choose a stronger PIN");
        }
    }

    private String generatePinSalt() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
