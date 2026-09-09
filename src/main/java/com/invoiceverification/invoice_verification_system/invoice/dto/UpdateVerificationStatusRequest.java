package com.invoiceverification.invoice_verification_system.invoice.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateVerificationStatusRequest {

    @NotBlank(message = "Verification status is required")
    private String verificationStatus;

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
