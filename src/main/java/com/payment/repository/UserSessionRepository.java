package com.payment.repository;

import com.payment.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.accessToken = null WHERE s.accessToken = :accessToken")
    void invalidateSession(String accessToken);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.accessToken = null WHERE s.user.id = :userId")
    void invalidateAllUserSessions(UUID userId);
}