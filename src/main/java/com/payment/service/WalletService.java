package com.payment.service;

import com.payment.dto.request.TransferWithPinRequest;
import com.payment.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    public PaymentResponse transfer(UUID userId, TransferWithPinRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(UUID.randomUUID().toString());
        response.setStatus("COMPLETED");
        response.setMessage("Transfer successful");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}