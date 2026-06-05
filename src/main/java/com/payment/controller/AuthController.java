
package com.payment.controller;

import com.payment.security.CurrentUser;
import com.payment.security.UserPrincipal;
import com.payment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import com.payment.model.User;
import com.payment.model.Wallet;
import com.payment.model.Role;
import com.payment.model.UserSession;
import com.payment.repository.WalletRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import com.payment.dto.response.LoginResponse;
import com.payment.dto.response.TokenResponse;
import com.payment.dto.request.ChangePasswordRequest;
import com.payment.dto.response.TwoFactorSetupResponse;
import com.payment.dto.request.TwoFactorVerifyRequest;
import com.payment.dto.response.UserProfileResponse;
import com.payment.dto.request.RegisterRequest;
import com.payment.dto.response.AuthResponse;
import com.payment.dto.request.LoginRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken,
            @CurrentUser UserPrincipal currentUser) {
        String token = authorization.substring(7);
        authService.logout(token, refreshToken);
        return ResponseEntity.ok().body("Logged out successfully");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<TokenResponse> refresh(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().body("Email verified successfully");
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok().body("Password changed successfully");
    }

    @PostMapping("/2fa/enable")
    @Operation(summary = "Enable two-factor authentication")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TwoFactorSetupResponse> enableTwoFactor(
            @CurrentUser UserPrincipal currentUser) {
        TwoFactorSetupResponse response = authService.enableTwoFactor(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify two-factor authentication")
    public ResponseEntity<?> verifyTwoFactor(@RequestBody TwoFactorVerifyRequest request) {
        authService.verifyTwoFactor(request);
        return ResponseEntity.ok().body("2FA verified successfully");
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(authService.getUserProfile(currentUser.getId()));
    }
}
