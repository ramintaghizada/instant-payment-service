package com.payment.config;

import com.payment.model.User;
import com.payment.model.Wallet;
import com.payment.repository.UserRepository;
import com.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create test user if not exists
        if (!userRepository.existsByPhoneNumber("+994501234567")) {
            log.info("Creating test user...");

            User user = new User();
            user.setUsername("+994501234567");
            user.setPhoneNumber("+994501234567");
            user.setEmail("test@example.com");
            user.setFullName("Test User");
            user.setPasswordHash(passwordEncoder.encode("Test@123456"));
            user.setSalt(UUID.randomUUID().toString());
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus(User.UserStatus.ACTIVE);
            user.setEmailVerified(true);
            user.setPhoneVerified(true);

            user = userRepository.save(user);

            // Create wallet with balance
            Wallet wallet = new Wallet();
            wallet.setWalletId("WLT_TEST123");
            wallet.setUser(user);
            wallet.setWalletName("Main Wallet");
            wallet.setBalance(new BigDecimal("1000.00"));
            wallet.setCurrency("AZN");
            wallet.setPrimary(true);
            wallet.setStatus(Wallet.WalletStatus.ACTIVE);
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setDailyLimit(new BigDecimal("5000.00"));
            wallet.setTransactionLimit(new BigDecimal("1000.00"));
            walletRepository.save(wallet);

            // Create merchant wallet
            Wallet merchantWallet = new Wallet();
            merchantWallet.setWalletId("WLT_ABCD1234");
            merchantWallet.setUser(user);
            merchantWallet.setWalletName("Merchant Wallet");
            merchantWallet.setBalance(new BigDecimal("5000.00"));
            merchantWallet.setCurrency("AZN");
            merchantWallet.setPrimary(false);
            merchantWallet.setStatus(Wallet.WalletStatus.ACTIVE);
            merchantWallet.setCreatedAt(LocalDateTime.now());
            walletRepository.save(merchantWallet);

            log.info("Test user created with wallet ID: {}", wallet.getWalletId());
            log.info("Wallet balance: {} AZN", wallet.getBalance());
        }
    }
}
