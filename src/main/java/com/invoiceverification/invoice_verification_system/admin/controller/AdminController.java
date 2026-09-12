package com.invoiceverification.invoice_verification_system.admin.controller;

import com.invoiceverification.invoice_verification_system.admin.dto.AdminInvoiceResponse;
import com.invoiceverification.invoice_verification_system.admin.dto.PageResponse;
import com.invoiceverification.invoice_verification_system.admin.entity.AdminUser;
import com.invoiceverification.invoice_verification_system.admin.service.AdminUserService;
import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.invoice.dto.UpdateVerificationStatusRequest;
import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import com.invoiceverification.invoice_verification_system.invoice.entity.VerificationStatus;
import com.invoiceverification.invoice_verification_system.invoice.repository.InvoiceRepository;
import com.invoiceverification.invoice_verification_system.invoice.repository.VerificationStatusHistoryRepository;
import com.invoiceverification.invoice_verification_system.invoice.service.S3Service;
import com.invoiceverification.invoice_verification_system.invoice.entity.VerificationStatusHistory;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminUserService admins; private final InvoiceRepository invoices; private final S3Service s3; private final VerificationStatusHistoryRepository history;
    public AdminController(AdminUserService admins, InvoiceRepository invoices, S3Service s3, VerificationStatusHistoryRepository history) { this.admins = admins; this.invoices = invoices; this.s3 = s3; this.history = history; }
    @GetMapping("/me") public Map<String, String> me(Authentication auth) { Brand brand = brand(auth); return Map.of("email", auth.getName(), "brandSlug", brand.getSlug(), "brandName", brand.getDisplayName()); }
    @GetMapping("/dashboard") public Map<String, Long> dashboard(Authentication auth) {
        Long brandId = brand(auth).getId();
        return Map.of("invoiceCount", invoices.countByBrandId(brandId),
                "pendingCount", invoices.countByBrandIdAndVerificationStatus(brandId, VerificationStatus.PENDING),
                "verifiedCount", invoices.countByBrandIdAndVerificationStatus(brandId, VerificationStatus.VERIFIED),
                "failedCount", invoices.countByBrandIdAndVerificationStatus(brandId, VerificationStatus.FAILED));
    }
    @GetMapping("/invoices") public PageResponse<AdminInvoiceResponse> list(Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.from(invoices.findAllByBrandId(brand(auth).getId(), PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))).map(AdminInvoiceResponse::from));
    }
    @Transactional
    @PatchMapping("/invoices/{invoiceId}/status") public AdminInvoiceResponse update(Authentication auth, @PathVariable Long invoiceId, @Valid @RequestBody UpdateVerificationStatusRequest request) {
        Invoice invoice = invoice(invoiceId, brand(auth));
        VerificationStatus previous = invoice.getVerificationStatus();
        try { invoice.setVerificationStatus(VerificationStatus.valueOf(request.getVerificationStatus().trim().toUpperCase())); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Verification status must be PENDING, VERIFIED, or FAILED"); }
        Invoice saved = invoices.save(invoice);
        VerificationStatusHistory change = new VerificationStatusHistory();
        change.setInvoice(saved); change.setOldStatus(previous.name()); change.setNewStatus(saved.getVerificationStatus().name());
        change.setChangedBy(auth.getName()); change.setReviewNote(request.getReviewNote()); history.save(change);
        return AdminInvoiceResponse.from(saved);
    }
    @GetMapping("/invoices/{invoiceId}/document-url") public Map<String, String> document(Authentication auth, @PathVariable Long invoiceId) { return Map.of("url", s3.createDownloadUrl(invoice(invoiceId, brand(auth)))); }
    private Brand brand(Authentication auth) { AdminUser admin = admins.requireAdmin(auth.getName()); return admin.getBrand(); }
    private Invoice invoice(Long id, Brand brand) { return invoices.findByIdAndBrandId(id, brand.getId()).orElseThrow(() -> new IllegalArgumentException("Invoice not found")); }
}
