
package com.payment.service;

import com.payment.model.Wallet;
import com.payment.model.User;
import com.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSecurityService {

    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder pinEncoder = new BCryptPasswordEncoder(8);
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void setWalletPin(UUID userId, String walletId, String pin) {
        Wallet wallet = walletRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        validatePinStrength(pin);

        String salt = generatePinSalt();
        String pinHash = pinEncoder.encode(pin + salt);

        wallet.setPinHash(pinHash);
        wallet.setPinSalt(salt);
        walletRepository.save(wallet);

        log.info("PIN set for wallet: {}", walletId);
    }

    public boolean verifyWalletPin(String walletId, String pin) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        String pinAttemptKey = "pin:attempts:" + walletId;
        Integer attempts = Integer.valueOf(redisTemplate.opsForValue().get(pinAttemptKey));

        if (attempts != null && attempts >= 3) {
            // Lock wallet after 3 failed attempts
            wallet.setStatus(Wallet.WalletStatus.LOCKED);
            walletRepository.save(wallet);
            throw new RuntimeException("Wallet locked due to too many failed PIN attempts");
        }

        boolean isValid = pinEncoder.matches(pin + wallet.getPinSalt(), wallet.getPinHash());

        if (!isValid) {
            int newAttempts = (attempts == null ? 1 : attempts + 1);
            redisTemplate.opsForValue().set(pinAttemptKey, String.valueOf(newAttempts),
                    Duration.ofMinutes(15));
            throw new RuntimeException("Invalid PIN");
        }

        // Reset attempts on success
        redisTemplate.delete(pinAttemptKey);
        return true;
    }

    private void validatePinStrength(String pin) {
        if (!pin.matches("\\d{4,6}")) {
            throw new RuntimeException("PIN must be 4-6 digits");
        }

        // Check for common patterns
        if (pin.equals("1234") || pin.equals("0000") || pin.equals("1111")) {
            throw new RuntimeException("PIN is too weak. Choose a stronger PIN");
        }
    }

    private String generatePinSalt() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
