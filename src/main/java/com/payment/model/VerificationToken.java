package com.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type; // EMAIL_VERIFICATION, PASSWORD_RESET, PHONE_VERIFICATION

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public VerificationToken() {
        this.token = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    public VerificationToken(User user, String type) {
        this();
        this.user = user;
        this.type = type;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
