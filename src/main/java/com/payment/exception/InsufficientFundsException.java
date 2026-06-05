
package com.payment.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(String walletId, java.math.BigDecimal amount) {
        super(String.format("Insufficient funds in wallet %s for amount %s", walletId, amount));
    }
}