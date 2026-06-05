
package com.payment.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String redirectUrl;
    private String fromWalletId;
    private String toWalletId;
    private Double amount;
    private String currency;
    
    // Helper method for builder pattern compatibility
    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }
    
    public static class PaymentResponseBuilder {
        private String transactionId;
        private String status;
        private String message;
        private LocalDateTime timestamp;
        private String redirectUrl;
        private String fromWalletId;
        private String toWalletId;
        private Double amount;
        private String currency;
        
        public PaymentResponseBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public PaymentResponseBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public PaymentResponseBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public PaymentResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public PaymentResponseBuilder redirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }
        
        public PaymentResponse build() {
            PaymentResponse response = new PaymentResponse();
            response.transactionId = this.transactionId;
            response.status = this.status;
            response.message = this.message;
            response.timestamp = this.timestamp;
            response.redirectUrl = this.redirectUrl;
            response.fromWalletId = this.fromWalletId;
            response.toWalletId = this.toWalletId;
            response.amount = this.amount;
            response.currency = this.currency;
            return response;
        }
    }
}