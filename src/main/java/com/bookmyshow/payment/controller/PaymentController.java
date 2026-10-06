package com.bookmyshow.payment.controller;

import com.bookmyshow.payment.dto.PaymentDtos;
import com.bookmyshow.payment.service.PaymentService;
import com.bookmyshow.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bookmyshow.user.entity.User;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDtos.PaymentResponse>> initiatePayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentDtos.InitiatePaymentRequest request) {
        
        PaymentDtos.PaymentResponse response = paymentService.initiatePayment(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDtos.PaymentDetailsResponse>> getPaymentDetails(
            @AuthenticationPrincipal User user,
            @PathVariable UUID paymentId) {
        
        PaymentDtos.PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<PaymentDtos.RefundResponse>> refundPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentDtos.RefundRequest request) {
        
        PaymentDtos.RefundResponse response = paymentService.refundPayment(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            HttpServletRequest request,
            @RequestBody String payload) {
        
        String signature = request.getHeader("X-Signature");
        
        // Parse payload to WebhookPayload
        // In real implementation, use ObjectMapper
        PaymentDtos.WebhookPayload webhookPayload = parseWebhookPayload(payload);
        
        paymentService.handleWebhook(webhookPayload, signature);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    private PaymentDtos.WebhookPayload parseWebhookPayload(String payload) {
        // Simplified - in real implementation use ObjectMapper
        return PaymentDtos.WebhookPayload.builder().build();
    }
}