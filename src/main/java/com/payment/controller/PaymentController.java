package com.payment.controller;

import com.payment.dto.request.PaymentRequest;
import com.payment.dto.response.PaymentResponse;
import com.payment.security.CurrentUser;
import com.payment.security.UserPrincipal;
import com.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between wallets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> transfer(@Valid @RequestBody PaymentRequest request,
            @CurrentUser UserPrincipal currentUser) {

        log.info("Transfer request from user: {}", currentUser.getId());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{transactionId}")
    @Operation(summary = "Get transaction status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getTransactionStatus(
            @PathVariable String transactionId) {

        PaymentResponse response = paymentService.getTransactionStatus(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/qr")
    @Operation(summary = "Process QR payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> processQRPayment(@RequestParam String qrCode,
            @RequestParam String fromWalletId, @CurrentUser UserPrincipal currentUser) {

        log.info("QR payment request from user: {}", currentUser.getId());

        // Parse QR code (simplified - in production, decode QR data)
        PaymentRequest request = new PaymentRequest();
        request.setFromWalletId(fromWalletId);
        request.setToWalletId("WLT_MERCHANT_" + qrCode.substring(0, 8));
        request.setAmount(new java.math.BigDecimal("100.00"));
        request.setCurrency("AZN");
        request.setDescription("QR Code Payment");
        request.setIdempotencyKey(java.util.UUID.randomUUID().toString());

        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}
