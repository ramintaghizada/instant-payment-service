package com.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    
    public void sendVerificationCode(String phoneNumber) {
        log.info("Sending verification code to: {}", phoneNumber);
    }
    
    public void sendPasswordChangedNotification(String phoneNumber) {
        log.info("Sending password changed SMS to: {}", phoneNumber);
    }
}