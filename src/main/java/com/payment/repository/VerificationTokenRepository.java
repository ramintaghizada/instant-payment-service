package com.payment.repository;

import com.payment.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken v WHERE v.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken v SET v.used = true WHERE v.token = :token")
    void markAsUsed(String token);
}
