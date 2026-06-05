
package com.payment.service;

import com.payment.dto.request.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    @Async
    public void sendPaymentNotification(PaymentRequest request) {
        log.info("Sending push notification to {}: Payment of {} {} to {}",
                request.getFromWalletId(), 
                request.getAmount(), 
                request.getCurrency(),
                request.getToWalletId());
        
        // In production: integrate with Firebase Cloud Messaging or APNS
        try {
            // Simulate API call
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Async
    public void sendInsufficientFundsNotification(PaymentRequest request) {
        log.warn("Insufficient funds notification sent to {}", request.getFromWalletId());
    }
}