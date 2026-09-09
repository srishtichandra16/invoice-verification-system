package com.invoiceverification.invoice_verification_system.invoice.controller;

import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceResponse;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceSubmission;
import com.invoiceverification.invoice_verification_system.invoice.dto.UpdateVerificationStatusRequest;
import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import com.invoiceverification.invoice_verification_system.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }


    @GetMapping("/test")
    public String test() {
        return invoiceService.checkInvoice();
    }


    @PostMapping("/api/invoices")
    public InvoiceResponse uploadInvoice(
            @Valid @ModelAttribute InvoiceSubmission submission,
            @RequestParam MultipartFile invoiceImage) {

        // Check file
        if (invoiceImage == null || invoiceImage.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invoice file cannot be empty"
            );
        }


        // Check file type
        String contentType = invoiceImage.getContentType();

        if (!"application/pdf".equals(contentType)
                && !"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)) {

            throw new IllegalArgumentException(
                    "Only PDF, JPEG and PNG files are allowed"
            );
        }


        // Send form data + file to service
        return invoiceService.processInvoice(
                submission,
                invoiceImage
        );
    }

    @PatchMapping("/api/invoices/{invoiceId}/status")
public InvoiceResponse updateVerificationStatus(
        @PathVariable Long invoiceId,
        @Valid @RequestBody UpdateVerificationStatusRequest request) {

    Invoice invoice = invoiceService.updateVerificationStatus(
            invoiceId,
            request
    );

    return new InvoiceResponse(
            invoice.getId(),
            invoice.getCompanyId(),
            invoice.getObjectKey(),
            invoice.getVerificationStatus().name(),
            invoice.getCreatedAt(),
            "Invoice verification status updated successfully"
    );


}

@GetMapping("/api/invoices/{invoiceId}")
public InvoiceResponse getInvoice(
        @PathVariable Long invoiceId) {

    Invoice invoice = invoiceService.getInvoiceById(invoiceId);

    return new InvoiceResponse(
            invoice.getId(),
            invoice.getCompanyId(),
            invoice.getObjectKey(),
            invoice.getVerificationStatus().name(),
            invoice.getCreatedAt(),
            "Invoice fetched successfully"
    );
}


}


