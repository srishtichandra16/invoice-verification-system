package com.invoiceverification.invoice_verification_system.invoice.dto;

import java.time.LocalDateTime;

public class InvoiceResponse {

    private Long invoiceId;
    private String message;
    private Long companyId;
    private String objectKey;
    private String verificationStatus;
    private LocalDateTime createdAt;

    public InvoiceResponse(
            Long invoiceId,
            Long companyId,
            String objectKey,
            String verificationStatus,
            LocalDateTime createdAt,
            String message) {

        this.invoiceId = invoiceId;
        this.companyId = companyId;
        this.objectKey = objectKey;
        this.verificationStatus = verificationStatus;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getObjectKey() {
        return objectKey;
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
