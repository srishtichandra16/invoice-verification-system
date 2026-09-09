package com.invoiceverification.invoice_verification_system.invoice.service;

import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceResponse;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceSubmission;
import com.invoiceverification.invoice_verification_system.invoice.dto.UpdateVerificationStatusRequest;
import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import com.invoiceverification.invoice_verification_system.invoice.entity.VerificationStatus;
import com.invoiceverification.invoice_verification_system.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final S3Service s3Service;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            S3Service s3Service) {

        this.invoiceRepository = invoiceRepository;
        this.s3Service = s3Service;
    }


    // Test method
    public String checkInvoice() {
        return "Invoice checked by service";
    }


    // Submit new invoice
    public InvoiceResponse processInvoice(
            InvoiceSubmission submission,
            MultipartFile invoiceImage) {

        // 1. Upload invoice document to S3
        String objectKey =
                s3Service.uploadInvoice(
                        invoiceImage,
                        submission.getCompanyId()
                );


        // 2. Create Invoice entity
        Invoice invoice = new Invoice();


        // 3. Company information
        invoice.setCompanyId(
                submission.getCompanyId()
        );


        // 4. Invoice information
        invoice.setInvoiceNumber(
                submission.getInvoiceNumber()
        );

        invoice.setReferenceNumber(
                submission.getReferenceNumber()
        );


        // 5. Product information
        invoice.setSku(
                submission.getSku()
        );

        invoice.setPurchaseDate(
                submission.getPurchaseDate()
        );

        invoice.setPurchasedFrom(
                submission.getPurchasedFrom()
        );


        // 6. Customer information
        invoice.setName(
                submission.getName()
        );

        invoice.setEmail(
                submission.getEmail()
        );

        invoice.setPhone(
                submission.getPhone()
        );

        invoice.setCountry(
                submission.getCountry()
        );


        // 7. Marketing preference
        invoice.setMarketingConsent(
                submission.getMarketingConsent()
        );


        // 8. Additional / brand-specific data
        invoice.setCustomData(
                submission.getCustomData()
        );


        // 9. S3 document location
        invoice.setObjectKey(
                objectKey
        );


        // 10. Initial verification status
        invoice.setVerificationStatus(
                VerificationStatus.PENDING
        );


        // 11. Creation timestamp
        invoice.setCreatedAt(
                LocalDateTime.now()
        );


        // 12. Save invoice to PostgreSQL
        invoiceRepository.save(invoice);


        // 13. Return response
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCompanyId(),
                invoice.getObjectKey(),
                invoice.getVerificationStatus().name(),
                invoice.getCreatedAt(),
                "Invoice submitted successfully"
        );
    }


    // Manually update invoice verification status
    public Invoice updateVerificationStatus(
            Long invoiceId,
            UpdateVerificationStatusRequest request) {

        // 1. Find invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invoice not found"
                        )
                );


        // 2. Convert String to VerificationStatus enum
        VerificationStatus newStatus;

        try {

            newStatus = VerificationStatus.valueOf(
                    request.getVerificationStatus().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid verification status. Allowed values: VERIFIED, FAILED"
            );
        }


        // 3. PENDING cannot be manually selected
        if (newStatus == VerificationStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Verification status cannot be changed to PENDING"
            );
        }


        // 4. Update status
        invoice.setVerificationStatus(
                newStatus
        );


        // 5. Save updated invoice
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceById(Long invoiceId) {

    return invoiceRepository.findById(invoiceId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Invoice not found"
                    )
            );
}

}
