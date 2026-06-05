
package com.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Wallet {
    
    @Id
    private String walletId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String walletName;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private String currency = "AZN";
    
    @Enumerated(EnumType.STRING)
    private WalletStatus status = WalletStatus.ACTIVE;
    
    @Column(name = "pin_hash")
    private String pinHash;
    
    @Column(name = "pin_salt")
    private String pinSalt;
    
    @Column(name = "is_primary")
    private boolean primary = false;
    
    @Column(name = "daily_limit")
    private BigDecimal dailyLimit = new BigDecimal("10000");
    
    @Column(name = "transaction_limit")
    private BigDecimal transactionLimit = new BigDecimal("5000");
    
    @Column(name = "daily_spent")
    private BigDecimal dailySpent = BigDecimal.ZERO;
    
    @Column(name = "last_reset_date")
    private LocalDateTime lastResetDate;
    
    @Version
    private Long version;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum WalletStatus {
        ACTIVE, FROZEN, CLOSED, LOCKED
    }
}