package com.invoiceverification.invoice_verification_system;

import com.invoiceverification.invoice_verification_system.admin.service.AdminUserService;
import com.invoiceverification.invoice_verification_system.brand.service.BrandService;
import com.invoiceverification.invoice_verification_system.config.SecurityConfig;
import com.invoiceverification.invoice_verification_system.invoice.controller.InvoiceController;
import com.invoiceverification.invoice_verification_system.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@Import(SecurityConfig.class)
class InvoiceFormWebTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private InvoiceService invoiceService;
    @MockitoBean private BrandService brandService;
    @MockitoBean private AdminUserService adminUserService;

    @Test
    void formAssetsArePublicAndDoNotExposeCompanyId() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"invoice-form\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("name=\"companyId\""))));
        mvc.perform(get("/styles.css")).andExpect(status().isOk());
        mvc.perform(get("/app.js")).andExpect(status().isOk());
        mvc.perform(get("/admin.html")).andExpect(status().isOk());
    }
}
