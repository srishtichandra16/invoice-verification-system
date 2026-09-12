# Multi-tenant Invoice Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver secure branded public invoice forms and tenant-isolated admin APIs/dashboard for Aigner and Roamer.

**Architecture:** Brand slug resolution occurs server-side for public submissions. Stateless HTTP Basic authentication resolves one active admin to one brand, and all admin data access is tenant-scoped. One static frontend adapts to register/admin URL paths.

**Tech Stack:** Java 21, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Flyway, AWS SDK v2 S3, HTML/CSS/JavaScript.

**Spec:** `docs/superpowers/specs/2026-09-12-multi-tenant-invoice-platform-design.md`

## Global Constraints

- Customers never send a company or brand ID.
- All admin invoice operations are scoped by authenticated admin brand ID.
- Production database/AWS credentials use environment variables only.
- Existing unrelated working-tree artifacts must remain untouched.

---

### Task 1: Tenant domain and public API

**Files:** Brand/Invoice entities, repositories, services, public controllers, tests.

- [ ] Write failing WebMvc tests for brand configuration and a branded multipart submission.
- [ ] Remove public company ID input and resolve `Brand` from the URL slug.
- [ ] Store a required brand relation and brand-specific S3 object key.
- [ ] Verify public API tests pass.

### Task 2: Admin identity and tenant-scoped APIs

**Files:** admin entity/repository/security classes, invoice history, admin DTOs/controller, tests.

- [ ] Write failing WebMvc tests proving anonymous access is rejected and cross-brand access is hidden.
- [ ] Implement BCrypt admin identity, HTTP Basic security, invoice list/detail/summary/status/document endpoints, and status audit history.
- [ ] Verify security and tenant tests pass.

### Task 3: Branded public/admin frontend

**Files:** static HTML/CSS/JS and forwarding controller.

- [ ] Write static-resource route tests for both brand register/admin paths.
- [ ] Replace company-ID UI with URL-driven brand configuration and submission.
- [ ] Add compact admin login, summary, invoice list, review, and document-open UI.
- [ ] Verify browser assets and WebMvc tests pass.

### Task 4: Production configuration and resilience

**Files:** Maven dependencies, profiles, Flyway migration, S3 service, validation/error handling, documentation.

- [ ] Add tests for rejected content signatures and S3 cleanup on save failure where practical.
- [ ] Add Flyway and production profile defaults, presigned URL support, stronger validation, and S3 cleanup.
- [ ] Run complete Maven test suite and secret scan.
