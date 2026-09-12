package com.invoiceverification.invoice_verification_system.invoice.service;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceResponse;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceSubmission;
import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import com.invoiceverification.invoice_verification_system.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InvoiceService {
    private final InvoiceRepository repository;
    private final S3Service s3;
    public InvoiceService(InvoiceRepository repository, S3Service s3) { this.repository = repository; this.s3 = s3; }
    @Transactional
    public InvoiceResponse processInvoice(InvoiceSubmission submission, MultipartFile document, Brand brand) {
        Invoice invoice = new Invoice();
        invoice.setBrand(brand);
        invoice.setInvoiceNumber(submission.getInvoiceNumber()); invoice.setReferenceNumber(submission.getReferenceNumber());
        invoice.setSku(submission.getSku()); invoice.setPurchaseDate(submission.getPurchaseDate()); invoice.setCustomData(submission.getCustomData());
        invoice.setName(submission.getName()); invoice.setEmail(submission.getEmail()); invoice.setPhone(submission.getPhone());
        invoice.setCountry(submission.getCountry()); invoice.setPurchasedFrom(submission.getPurchasedFrom()); invoice.setMarketingConsent(submission.getMarketingConsent());
        String objectKey = s3.uploadInvoice(document, brand);
        invoice.setObjectKey(objectKey);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) s3.deleteInvoiceQuietly(objectKey);
            }
        });
        Invoice saved = repository.saveAndFlush(invoice);
        return new InvoiceResponse(saved.getId(), brand.getSlug(), saved.getVerificationStatus().name(), saved.getCreatedAt(), "Invoice submitted successfully");
    }
}
