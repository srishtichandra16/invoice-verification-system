package com.invoiceverification.invoice_verification_system.invoice.dto;

import java.time.LocalDateTime;

public class InvoiceResponse {

    private Long invoiceId;
    private String message;
    private String brandSlug;
    private String verificationStatus;
    private LocalDateTime createdAt;

    public InvoiceResponse(
            Long invoiceId,
            String brandSlug,
            String verificationStatus,
            LocalDateTime createdAt,
            String message) {

        this.invoiceId = invoiceId;
        this.brandSlug = brandSlug;
        this.verificationStatus = verificationStatus;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getBrandSlug() {
        return brandSlug;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
}
