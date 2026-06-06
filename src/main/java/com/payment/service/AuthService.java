
package com.payment.service;

import com.payment.model.User;
import com.payment.model.UserSession;
import com.payment.model.Wallet;
import com.payment.repository.RoleRepository;
import com.payment.repository.UserRepository;
import com.payment.repository.UserSessionRepository;
import com.payment.repository.WalletRepository;
import com.payment.security.JwtTokenProvider;
import com.payment.security.TokenBlacklistService;
import com.payment.dto.response.TokenResponse;
import com.payment.dto.response.TwoFactorSetupResponse;
import com.payment.dto.response.UserProfileResponse;
import com.payment.dto.response.WalletResponse;
import com.payment.dto.response.AuthResponse;
import com.payment.dto.response.LoginResponse;
import com.payment.dto.request.ChangePasswordRequest;
import com.payment.dto.request.LoginRequest;
import com.payment.dto.request.RegisterRequest;
import com.payment.dto.request.TwoFactorRequest;
import com.payment.dto.request.TwoFactorVerifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Set;
import com.payment.model.VerificationToken;
import com.payment.repository.VerificationTokenRepository;
import com.payment.util.TOTPUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final AuditService auditService;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate phone number uniqueness
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Generate salt and hash password
        String salt = generateSalt();
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Create user
        User user = new User();
        user.setUsername(request.getPhoneNumber());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setCreatedAt(LocalDateTime.now());

        // Assign default role
        user.setRoles(Set.of(roleRepository.findByName("ROLE_USER").orElseThrow()));

        user = userRepository.save(user);

        // Create default wallet
        Wallet wallet = new Wallet();
        wallet.setWalletId(generateWalletId());
        wallet.setUser(user);
        wallet.setWalletName("Main Wallet");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("AZN");
        wallet.setPrimary(true);
        wallet.setStatus(Wallet.WalletStatus.ACTIVE);
        walletRepository.save(wallet);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), generateVerificationToken(user));

        // Send SMS verification
        smsService.sendVerificationCode(user.getPhoneNumber());

        auditService.logUserAction(user.getId(), "REGISTER", "User registered successfully");

        return AuthResponse.builder().success(true)
                .message("Registration successful. Please verify your email and phone number.")
                .userId(user.getId()).build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if account is locked
        if (user.getFailedLoginAttempts() >= 5) {
            throw new RuntimeException("Account locked. Contact support.");
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.getPhoneNumber(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(httpRequest.getRemoteAddr());
            userRepository.save(user);

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            // Create session
            UserSession session = new UserSession();
            session.setUser(user);
            session.setAccessToken(accessToken);
            session.setRefreshToken(refreshToken);
            session.setIpAddress(httpRequest.getRemoteAddr());
            session.setUserAgent(httpRequest.getHeader("User-Agent"));
            session.setExpiresAt(LocalDateTime.now().plusHours(24));
            userSessionRepository.save(session);

            List<Wallet> wallets = walletRepository.findByUserId(user.getId());
            String primaryWalletId = wallets.stream().filter(Wallet::isPrimary).findFirst()
                    .map(Wallet::getWalletId).orElse(null);

            List<LoginResponse.WalletInfo> walletInfoList = wallets.stream()
                    .map(wallet -> LoginResponse.WalletInfo.builder().walletId(wallet.getWalletId())
                            .walletName(wallet.getWalletName()).currency(wallet.getCurrency())
                            .isPrimary(wallet.isPrimary()).build())
                    .collect(Collectors.toList());

            auditService.logUserAction(user.getId(), "LOGIN",
                    "User logged in from IP: " + httpRequest.getRemoteAddr());

            return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
                    .tokenType("Bearer").expiresIn(tokenProvider.getTokenRemainingTime(accessToken))
                    .userId(user.getId()).fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber()).email(user.getEmail())
                    .emailVerified(user.isEmailVerified()).phoneVerified(user.isPhoneVerified())
                    .twoFactorEnabled(user.isTwoFactorEnabled()).primaryWalletId(primaryWalletId)
                    .wallets(walletInfoList).build();

        } catch (Exception e) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Blacklist tokens
        long accessTokenExpiry = tokenProvider.getTokenRemainingTime(accessToken);
        long refreshTokenExpiry = tokenProvider.getTokenRemainingTime(refreshToken);

        tokenBlacklistService.blacklistToken(accessToken, accessTokenExpiry);
        tokenBlacklistService.blacklistToken(refreshToken, refreshTokenExpiry);

        // Invalidate session
        userSessionRepository.invalidateSession(accessToken);

        UUID userId = tokenProvider.getUserIdFromToken(accessToken);
        auditService.logUserAction(userId, "LOGOUT", "User logged out");
    }

    public void verifyEmail(String token) {
        // Implementation for email verification
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Invalidate all sessions except current
        userSessionRepository.invalidateAllUserSessions(userId);

        auditService.logUserAction(userId, "PASSWORD_CHANGE", "Password changed successfully");

        // Send notification
        emailService.sendPasswordChangedNotification(user.getEmail());
        smsService.sendPasswordChangedNotification(user.getPhoneNumber());
    }

    public void enableTwoFactor(UUID userId, TwoFactorRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate TOTP secret
        String secret = TOTPUtil.generateSecret();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        // Generate QR code for Google Authenticator
        String qrCodeUrl = TOTPUtil.getQRCodeUrl(user.getEmail(), secret);

        auditService.logUserAction(userId, "2FA_ENABLED", "Two-factor authentication enabled");
    }

    public TwoFactorSetupResponse enableTwoFactor(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate TOTP secret
        String secret = TOTPUtil.generateSecret();
        String qrCodeUrl = TOTPUtil.getQRCodeUrl(user.getEmail(), secret);

        // Enable 2FA for the user
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        log.info("Two-factor authentication enabled for user: {}", userId);

        // Return response with QR code
        return TwoFactorSetupResponse.builder().secret(secret).qrCodeUrl(qrCodeUrl)
                .message("Scan the QR code with Google Authenticator or similar app").build();
    }

    public void verifyTwoFactor(TwoFactorVerifyRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("Two-factor authentication not enabled for this user");
        }

        boolean isValid = TOTPUtil.verifyCode(user.getTwoFactorSecret(), request.getCode());

        if (!isValid) {
            throw new RuntimeException("Invalid two-factor authentication code");
        }

        log.info("Two-factor authentication verified for user: {}", user.getPhoneNumber());
    }

    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's wallets
        List<WalletResponse> walletResponses = walletRepository.findByUserId(userId).stream()
                .map(wallet -> WalletResponse.builder().walletId(wallet.getWalletId())
                        .walletName(wallet.getWalletName()).balance(wallet.getBalance())
                        .currency(wallet.getCurrency()).status(wallet.getStatus().toString())
                        .isPrimary(wallet.isPrimary()).build())
                .collect(Collectors.toList());

        return UserProfileResponse.builder().userId(user.getId()).fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber()).email(user.getEmail())
                .emailVerified(user.isEmailVerified()).phoneVerified(user.isPhoneVerified())
                .twoFactorEnabled(user.isTwoFactorEnabled()).wallets(walletResponses).build();
    }

    public TokenResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new authentication
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), null);

        String newAccessToken = tokenProvider.generateAccessToken(authentication);

        return TokenResponse.builder().accessToken(newAccessToken).refreshToken(refreshToken)
                .tokenType("Bearer").expiresIn(tokenProvider.getTokenRemainingTime(newAccessToken))
                .build();
    }

    private String generateSalt() {
        return UUID.randomUUID().toString();
    }

    private String generateWalletId() {
        return "WLT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateVerificationToken(User user) {
        return UUID.randomUUID().toString();
    }
}
