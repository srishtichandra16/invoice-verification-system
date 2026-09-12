# Invoice verification system

One Spring Boot application for multiple brands. Customers use a brand-specific public link; each brand administrator sees only their own invoices.

## Local start

1. Start PostgreSQL and make sure the database in `application-dev.yaml` exists.
2. From this folder, run `./mvnw spring-boot:run`.
3. Open `http://localhost:8080/aigner/register` or `http://localhost:8080/roamer/register`.

The public form takes the brand from its URL. It never accepts a `companyId` from a customer.

## Create local admin accounts

No default password is stored in the repository. Set long, unique local passwords in the shell that starts the app:

```bash
export SEED_ADMINS=true
export ADMIN_BRAND_SLUG='aigner'
export ADMIN_EMAIL='admin@aigner.local'
export ADMIN_PASSWORD='replace-with-a-long-unique-password'
./mvnw spring-boot:run
```

The password must be at least 14 characters. It is BCrypt-hashed before being stored. Then open `http://localhost:8080/aigner/admin`. After the first successful start, set `SEED_ADMINS=false` and remove the seed password from your environment.

## Production environment

Run with `SPRING_PROFILES_ACTIVE=prod` and set all of these only in the deployment platform’s secret/environment settings, never in Git:

```text
DB_URL=jdbc:postgresql://host:5432/invoice_db?sslmode=require
DB_USERNAME=...
DB_PASSWORD=...
AWS_REGION=ap-south-1
AWS_S3_BUCKET=...
APP_BOOTSTRAP_BRANDS=aigner
```

Production must terminate HTTPS before the app because administrator authentication uses HTTP Basic. The app sends Basic credentials only in memory; it does not save them in browser storage. S3 objects should remain private, and the document endpoint returns a five-minute signed URL only after brand-bound admin authorization.

## Adding a brand

Add the lowercase slug to `APP_BOOTSTRAP_BRANDS` (comma-separated), restart, and create an admin user for it through the controlled seed process. Brands omitted from that setting are made inactive. Then share links in this format:

```text
https://your-domain.example/new-brand/register
https://your-domain.example/new-brand/admin
```

## Current security controls

- Public API is limited to brand lookup and invoice submission.
- Customer submissions are associated with the server-resolved brand, not a client-supplied ID.
- Admin APIs require an `ADMIN` account and query by that account’s brand ID.
- Passwords use BCrypt; no credentials are committed.
- Uploads are limited to 10 MB and check MIME type plus file signature.
- Verification-status changes have an audit-history record.

The initial production schema is created idempotently from `schema.sql`; Hibernate then validates it. Before making later schema changes, introduce numbered Flyway migrations rather than editing a live table manually. Deployment instructions are in `deploy/aws/README.md`.
