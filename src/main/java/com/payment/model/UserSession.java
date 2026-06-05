package com.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Data
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessToken;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String refreshToken;
    
    private String ipAddress;
    
    @Column(columnDefinition = "TEXT")
    private String userAgent;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt;
}