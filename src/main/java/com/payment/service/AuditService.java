
package com.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.dto.request.PaymentRequest;
import com.payment.model.Transaction;
import com.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void recordTransaction(PaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setFromWallet(request.getFromWalletId());
        transaction.setToWallet(request.getToWalletId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus("COMPLETED");
        transaction.setDescription(request.getDescription());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setReferenceId(UUID.randomUUID().toString());
        
        transactionRepository.save(transaction);
        log.info("Transaction recorded: {}", transaction.getId());
        
        // Send to audit Kafka topic for compliance
        try {
            String auditJson = objectMapper.writeValueAsString(transaction);
            kafkaTemplate.send("audit-events", auditJson);
        } catch (Exception e) {
            log.error("Failed to send audit event", e);
        }
    }
    
    @Transactional
    public void recordFailedTransaction(PaymentRequest request, String reason) {
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setFromWallet(request.getFromWalletId());
        transaction.setToWallet(request.getToWalletId());
        transaction.setAmount(request.getAmount());
        transaction.setStatus("FAILED");
        transaction.setDescription(reason);
        
        transactionRepository.save(transaction);
        log.warn("Failed transaction recorded: {} - {}", transaction.getId(), reason);
    }
    
    public void logUserAction(UUID userId, String action, String details) {
        log.info("User action - User: {}, Action: {}, Details: {}", userId, action, details);
    }
}