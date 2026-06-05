package com.payment.service;

import com.payment.dto.request.PaymentRequest;
import com.payment.dto.response.PaymentResponse;
import com.payment.model.Transaction;
import com.payment.model.Wallet;
import com.payment.repository.TransactionRepository;
import com.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment from {} to {} for amount {}", request.getFromWalletId(),
                request.getToWalletId(), request.getAmount());

        // Validate wallets exist
        Wallet fromWallet = walletRepository.findById(request.getFromWalletId())
                .orElseThrow(() -> new RuntimeException(
                        "Source wallet not found: " + request.getFromWalletId()));

        Wallet toWallet = walletRepository.findById(request.getToWalletId())
                .orElseThrow(() -> new RuntimeException(
                        "Destination wallet not found: " + request.getToWalletId()));

        // Check sufficient balance
        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException(
                    "Insufficient balance. Available: " + fromWallet.getBalance());
        }

        // Check daily limit
        if (request.getAmount().compareTo(fromWallet.getDailyLimit()) > 0) {
            throw new RuntimeException("Amount exceeds daily limit: " + fromWallet.getDailyLimit());
        }

        // Perform transfer
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setFromWallet(request.getFromWalletId());
        transaction.setToWallet(request.getToWalletId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus("COMPLETED");
        transaction.setDescription(request.getDescription());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setReferenceId(UUID.randomUUID().toString());

        transactionRepository.save(transaction);

        // Audit log
        auditService.logUserAction(null, "PAYMENT",
                String.format("Payment of %s %s from %s to %s", request.getAmount(),
                        request.getCurrency(), request.getFromWalletId(), request.getToWalletId()));

        log.info("Payment completed successfully. Transaction ID: {}", transaction.getId());

        return PaymentResponse.builder().transactionId(transaction.getId().toString())
                .status("COMPLETED").message("Payment processed successfully")
                .timestamp(LocalDateTime.now()).fromWalletId(request.getFromWalletId())
                .toWalletId(request.getToWalletId()).amount(request.getAmount().doubleValue())
                .currency(request.getCurrency()).build();
    }

    public PaymentResponse getTransactionStatus(String transactionId) {
        Transaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        return PaymentResponse.builder().transactionId(transaction.getId().toString())
                .status(transaction.getStatus()).message("Transaction retrieved successfully")
                .timestamp(transaction.getCompletedAt()).fromWalletId(transaction.getFromWallet())
                .toWalletId(transaction.getToWallet()).amount(transaction.getAmount().doubleValue())
                .currency(transaction.getCurrency()).build();
    }
}
