
package com.payment.exception;

import com.payment.dto.response.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<PaymentResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus("FAILED");
        response.setMessage(ex.getMessage());
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentResponse> handleIllegalArgument(IllegalArgumentException ex) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus("FAILED");
        response.setMessage(ex.getMessage());
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentResponse> handleGenericException(Exception ex) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus("ERROR");
        response.setMessage("Internal server error: " + ex.getMessage());
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}