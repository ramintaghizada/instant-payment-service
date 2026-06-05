package com.payment.util;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class TOTPUtil {

    private static final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private static final TimeProvider timeProvider = new SystemTimeProvider();
    private static final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private static final CodeVerifier verifier =
            new DefaultCodeVerifier(codeGenerator, timeProvider);

    public static String generateSecret() {
        return secretGenerator.generate();
    }

    public static String getQRCodeUrl(String email, String secret) {
        QrData data = new QrData.Builder().label(email).secret(secret).issuer("InstantPayment")
                .algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData;
        try {
            imageData = generator.generate(data);
            return "data:image/png;base64,"
                    + java.util.Base64.getEncoder().encodeToString(imageData);
        } catch (QrGenerationException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public static boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
