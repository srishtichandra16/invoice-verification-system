package com.invoiceverification.invoice_verification_system;

import com.invoiceverification.invoice_verification_system.security.RequestRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestRateLimitFilterTests {
    @Test void publicUploadsAreLimitedPerClientAddress() throws Exception {
        RequestRateLimitFilter filter = new RequestRateLimitFilter();
        for (int attempt = 1; attempt <= 11; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/public/brands/aigner/invoices");
            request.setRemoteAddr("192.0.2.10");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> { });
            assertThat(response.getStatus()).isEqualTo(attempt <= 10 ? 200 : 429);
        }
    }

    @Test void repeatedFailedAdminRequestsAreLimited() throws Exception {
        RequestRateLimitFilter filter = new RequestRateLimitFilter();
        for (int attempt = 1; attempt <= 11; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/me");
            request.setRemoteAddr("192.0.2.20");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, (ignoredRequest, servletResponse) -> ((jakarta.servlet.http.HttpServletResponse) servletResponse).setStatus(401));
            assertThat(response.getStatus()).isEqualTo(attempt <= 10 ? 401 : 429);
        }
    }
}
