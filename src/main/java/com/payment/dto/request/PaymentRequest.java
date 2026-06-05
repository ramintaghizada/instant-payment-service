package com.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "From wallet ID is required")
    private String fromWalletId;

    @NotBlank(message = "To wallet ID is required")
    private String toWalletId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Minimum amount is 0.01")
    @DecimalMax(value = "10000.00", message = "Maximum amount is 10000.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency = "AZN";

    private String description;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private String qrCodeData;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Getters
    public String getFromWalletId() {
        return fromWalletId;
    }

    public String getToWalletId() {
        return toWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    // Setters
    public void setFromWalletId(String fromWalletId) {
        this.fromWalletId = fromWalletId;
    }

    public void setToWalletId(String toWalletId) {
        this.toWalletId = toWalletId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
