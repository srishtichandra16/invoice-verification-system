# Multi-tenant Invoice Platform Design

## Goal

Run Aigner and Roamer from one Spring Boot application and PostgreSQL database. Customers submit anonymously through separate branded URLs. Only an admin assigned to a brand can view, download, or update that brand's invoices.

## Public flow

`/{brand}/register` serves one shared form. The browser reads the slug from the URL, loads public brand configuration, and submits to `POST /api/public/brands/{brand}/invoices`. The server resolves the active `Brand`; it never accepts a customer-provided company or brand ID.

## Admin flow

`/{brand}/admin` serves a shared admin dashboard. An admin supplies email and password in the browser and the dashboard uses HTTPS HTTP Basic authentication for each admin API request. Credentials are kept only in JavaScript memory. Spring Security authenticates the account using BCrypt and scopes every repository read, update, and presigned document URL to the authenticated admin's brand.

## Data model

`Brand` owns invoices and admin users. `Invoice.brand_id` is required for new public submissions. `AdminUser` contains a BCrypt hash, active flag, and one brand. Invoice status changes include an optional review note and create an `InvoiceStatusHistory` row containing the previous and new status, actor, note, and timestamp.

## Security and operational constraints

- Public access is limited to brand configuration and submission endpoints.
- `/api/admin/**` requires an active ADMIN account.
- Database and S3 credentials are environment-provided for production; no real secret is committed.
- S3 objects are private, placed under `brands/{slug}/invoices/`, and exposed only via short-lived presigned URLs after authorization.
- Uploaded files are size-limited and verified by MIME type plus file signature.
- Production uses `prod` profile, Flyway, `ddl-auto: validate`, and HTTPS at the deployment proxy/load balancer.

## Non-goals

This change does not deploy the app, create cloud accounts, configure DNS/HTTPS certificates, or invent real production credentials. Those values are configured in the chosen host's secret manager.
