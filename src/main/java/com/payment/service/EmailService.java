package com.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    public void sendVerificationEmail(String email, String token) {
        log.info("Sending verification email to {} with token: {}", email, token);
        // Implement actual email sending here
    }
    
    public void sendPasswordChangedNotification(String email) {
        log.info("Sending password changed notification to: {}", email);
    }
}