package com.payment.repository;

import com.payment.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByWalletIdAndUserId(String walletId, UUID userId);
    Optional<Wallet> findPrimaryWalletByUserId(UUID userId);
}