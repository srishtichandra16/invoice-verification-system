package com.invoiceverification.invoice_verification_system;

import com.invoiceverification.invoice_verification_system.config.SecurityConfig;
import com.invoiceverification.invoice_verification_system.invoice.controller.InvoiceController;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceResponse;
import com.invoiceverification.invoice_verification_system.invoice.dto.InvoiceSubmission;
import com.invoiceverification.invoice_verification_system.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@Import(SecurityConfig.class)
class InvoiceFormWebTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private InvoiceService invoiceService;

    @Test
    void formAndAssetsAreAccessibleWithoutLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"invoice-form\"")));
        mvc.perform(get("/styles.css")).andExpect(status().isOk());
        mvc.perform(get("/app.js")).andExpect(status().isOk());
        mvc.perform(get("/favicon.svg")).andExpect(status().isOk());
    }

    @Test
    void multipartFormBindsEveryFieldAndReturnsServerReceipt() throws Exception {
        when(invoiceService.processInvoice(any(), any())).thenReturn(new InvoiceResponse(
                42L, 1L, "invoices/test.pdf", "PENDING", LocalDateTime.of(2026, 9, 10, 12, 0),
                "Invoice submitted successfully"));
        mvc.perform(multipart("/api/invoices")
                        .file(new MockMultipartFile("invoiceImage", "invoice.pdf", "application/pdf", new byte[]{1, 2, 3}))
                        .param("companyId", "1").param("purchaseDate", "2026-09-01")
                        .param("invoiceNumber", "INV-FORM-TEST").param("referenceNumber", "REF-TEST")
                        .param("sku", "WATCH-TEST").param("purchasedFrom", "Test store")
                        .param("name", "Test Customer").param("email", "test@example.com")
                        .param("phone", "+91 1234567890").param("country", "India")
                        .param("customData", "{\"watchReference\":\"TEST\"}")
                        .param("marketingConsent", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.invoiceId").value(42))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
        ArgumentCaptor<InvoiceSubmission> submission = ArgumentCaptor.forClass(InvoiceSubmission.class);
        ArgumentCaptor<MultipartFile> document = ArgumentCaptor.forClass(MultipartFile.class);
        verify(invoiceService).processInvoice(submission.capture(), document.capture());
        InvoiceSubmission actual = submission.getValue();
        assertThat(actual.getCompanyId()).isEqualTo(1L);
        assertThat(actual.getPurchaseDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(actual.getInvoiceNumber()).isEqualTo("INV-FORM-TEST");
        assertThat(actual.getReferenceNumber()).isEqualTo("REF-TEST");
        assertThat(actual.getSku()).isEqualTo("WATCH-TEST");
        assertThat(actual.getPurchasedFrom()).isEqualTo("Test store");
        assertThat(actual.getName()).isEqualTo("Test Customer");
        assertThat(actual.getEmail()).isEqualTo("test@example.com");
        assertThat(actual.getPhone()).isEqualTo("+91 1234567890");
        assertThat(actual.getCountry()).isEqualTo("India");
        assertThat(actual.getCustomData()).isEqualTo("{\"watchReference\":\"TEST\"}");
        assertThat(actual.getMarketingConsent()).isFalse();
        assertThat(document.getValue().getOriginalFilename()).isEqualTo("invoice.pdf");
        assertThat(document.getValue().getBytes()).containsExactly(1, 2, 3);
    }

    @Test
    void missingRequiredFieldsReturnReadableErrorsWithoutUploading() throws Exception {
        mvc.perform(multipart("/api/invoices")
                        .file(new MockMultipartFile("invoiceImage", "invoice.pdf", "application/pdf", new byte[]{1})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.companyId").value("Company ID is required"))
                .andExpect(jsonPath("$.fields.purchaseDate").value("Purchase date is required"));
        verifyNoInteractions(invoiceService);
    }

    @Test
    void unsupportedDocumentReturnsReadableErrorWithoutUploading() throws Exception {
        mvc.perform(multipart("/api/invoices")
                        .file(new MockMultipartFile("invoiceImage", "file.txt", "text/plain", new byte[]{1}))
                        .param("companyId", "1").param("purchaseDate", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only PDF, JPEG and PNG files are allowed"));
        verifyNoInteractions(invoiceService);
    }

    @Test
    void emptyDocumentReturnsReadableError() throws Exception {
        mvc.perform(multipart("/api/invoices")
                        .file(new MockMultipartFile("invoiceImage", "invoice.pdf", "application/pdf", new byte[0]))
                        .param("companyId", "1").param("purchaseDate", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invoice file cannot be empty"));
        verifyNoInteractions(invoiceService);
    }

    @Test
    void missingDocumentReturnsReadableError() throws Exception {
        mvc.perform(multipart("/api/invoices").param("companyId", "1").param("purchaseDate", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please attach your invoice document."));
        verifyNoInteractions(invoiceService);
    }
}
