package com.invoiceverification.invoice_verification_system;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.brand.service.BrandService;
import com.invoiceverification.invoice_verification_system.admin.service.AdminUserService;
import com.invoiceverification.invoice_verification_system.config.SecurityConfig;
import com.invoiceverification.invoice_verification_system.invoice.controller.InvoiceController;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceResponse;
import com.invoiceverification.invoice_verification_system.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@Import(SecurityConfig.class)
class BrandedInvoiceWebTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    void publicSubmissionUsesBrandFromUrlAndDoesNotNeedCompanyId() throws Exception {
        Brand aigner = new Brand("aigner", "AIGNER");
        when(brandService.getActiveBrandBySlug("aigner")).thenReturn(aigner);
        when(invoiceService.processInvoice(any(), any(), any())).thenReturn(new InvoiceResponse(
                42L, "aigner", "PENDING", LocalDateTime.of(2026, 9, 12, 10, 0),
                "Invoice submitted successfully"));

        mvc.perform(multipart("/api/public/brands/aigner/invoices")
                        .file(new MockMultipartFile("invoiceImage", "invoice.pdf", "application/pdf", "%PDF-1.7".getBytes()))
                        .param("purchaseDate", "2026-09-01")
                        .param("invoiceNumber", "INV-42")
                        .param("marketingConsent", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(42))
                .andExpect(jsonPath("$.brandSlug").value("aigner"));

        ArgumentCaptor<Brand> brand = ArgumentCaptor.forClass(Brand.class);
        verify(invoiceService).processInvoice(any(), any(), brand.capture());
        assertThat(brand.getValue()).isSameAs(aigner);
    }

    @Test
    void publicSubmissionRejectsAFileWhoseContentDoesNotMatchItsMimeType() throws Exception {
        mvc.perform(multipart("/api/public/brands/aigner/invoices")
                        .file(new MockMultipartFile("invoiceImage", "not-a-pdf.pdf", "application/pdf", "not actually a PDF".getBytes()))
                        .param("purchaseDate", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The uploaded file content does not match a PDF, JPEG, or PNG document."));
    }
}
