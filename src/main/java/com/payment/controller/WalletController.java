
package com.payment.controller;

import com.payment.security.CurrentUser;
import com.payment.security.UserPrincipal;
import com.payment.service.WalletService;
import com.payment.service.WalletSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.payment.dto.response.PaymentResponse;
import com.payment.dto.request.SetPinRequest;
import com.payment.dto.request.VerifyPinRequest;
import com.payment.dto.request.TransferWithPinRequest;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class WalletController {

    private final WalletService walletService;
    private final WalletSecurityService walletSecurityService;

    @PostMapping("/pin/set")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set PIN for wallet")
    public ResponseEntity<?> setWalletPin(@CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SetPinRequest request) {
        walletSecurityService.setWalletPin(currentUser.getId(), request.getWalletId(),
                request.getPin());
        return ResponseEntity.ok().body("PIN set successfully");
    }

    @PostMapping("/pin/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify wallet PIN")
    public ResponseEntity<?> verifyWalletPin(@Valid @RequestBody VerifyPinRequest request) {
        boolean isValid =
                walletSecurityService.verifyWalletPin(request.getWalletId(), request.getPin());
        return ResponseEntity.ok().body(isValid ? "PIN verified" : "Invalid PIN");
    }

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Transfer money with PIN verification")
    public ResponseEntity<PaymentResponse> transferWithPin(@CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TransferWithPinRequest request) {
        // Verify PIN first
        walletSecurityService.verifyWalletPin(request.getFromWalletId(), request.getPin());

        // Process transfer
        PaymentResponse response = walletService.transfer(currentUser.getId(), request);
        return ResponseEntity.accepted().body(response);
    }
}
