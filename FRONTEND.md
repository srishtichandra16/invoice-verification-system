# Invoice registration form

Run the existing Spring Boot application with `./mvnw spring-boot:run`, then open
http://localhost:8080/. No Node installation, separate frontend server, or CORS
configuration is needed. Restart an already-running application after this change.

The HTML, CSS, and JavaScript live in `src/main/resources/static/`. The form sends
`FormData` to the same-origin `POST /api/invoices` endpoint using the DTO's exact
field names and the `invoiceImage` file part. The browser sets the multipart
boundary. Marketing consent defaults to false. Company ID, purchase date, and an
invoice document are required. Other fields are optional. Text fields are limited
to the existing database columns' 255-character capacity.

PDF, JPEG, and PNG files up to 10 MiB are accepted; the server's overall multipart
request limit is 12 MiB to accommodate the other fields. The success receipt uses
the actual `invoiceId`, `companyId`, and `verificationStatus` returned by the API.
Network failures retain the form. A timeout can occur after the server accepts a
submission, so the UI asks users to check before submitting again.

Actual submissions need the existing PostgreSQL database and valid AWS S3 upload
credentials. No AWS credentials are exposed to the frontend. API authorization
for `/api/invoices/**` is unchanged; only this page's static resources and error
dispatches are additionally allowed.

Run `./mvnw test` to verify the application and web integration. `InvoiceFormWebTests`
uses a mocked invoice service to verify multipart field binding, anonymous static
access, and validation responses without creating invoices or uploading to S3.

The visual treatment is Aigner-inspired; this project does not assert an official
brand affiliation.
