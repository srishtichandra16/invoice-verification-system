package com.invoiceverification.invoice_verification_system.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateVerificationStatusRequest {

    @NotBlank(message = "Verification status is required")
    private String verificationStatus;
    @Size(max = 1000, message = "Review note must be 1000 characters or fewer")
    private String reviewNote;

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}
