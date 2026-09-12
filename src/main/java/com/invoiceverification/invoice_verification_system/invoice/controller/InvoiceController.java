package com.invoiceverification.invoice_verification_system.invoice.controller;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.brand.service.BrandService;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceResponse;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceSubmission;
import com.invoiceverification.invoice_verification_system.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/public/brands/{brandSlug}/invoices")
public class InvoiceController {
    private static final long MAX_FILE_BYTES = 10 * 1024 * 1024;
    private final InvoiceService invoices;
    private final BrandService brands;
    public InvoiceController(InvoiceService invoices, BrandService brands) { this.invoices = invoices; this.brands = brands; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public InvoiceResponse submit(@PathVariable String brandSlug, @Valid @ModelAttribute InvoiceSubmission submission,
                                  @org.springframework.web.bind.annotation.RequestParam("invoiceImage") MultipartFile invoiceImage) throws IOException {
        validateDocument(invoiceImage);
        Brand brand = brands.getActiveBrandBySlug(brandSlug);
        return invoices.processInvoice(submission, invoiceImage, brand);
    }
    private void validateDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Invoice file cannot be empty");
        if (file.getSize() > MAX_FILE_BYTES) throw new IllegalArgumentException("Invoice files must be 10 MB or smaller");
        String type = file.getContentType();
        byte[] bytes = file.getInputStream().readNBytes(12);
        boolean pdf = "application/pdf".equals(type) && bytes.length >= 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
        boolean jpeg = "image/jpeg".equals(type) && bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        boolean png = "image/png".equals(type) && bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
        if (!pdf && !jpeg && !png) throw new IllegalArgumentException("The uploaded file content does not match a PDF, JPEG, or PNG document.");
    }
}
