# AWS deployment order (Aigner first)

Do not point a public domain at the application until the readiness check and HTTPS work.

1. Create a private S3 bucket with Block Public Access enabled and attach the least-privilege policy in `ec2-s3-policy.example.json` to the EC2 instance role.
2. Create PostgreSQL RDS in the same VPC. Keep it private; its security group should accept port 5432 only from the application EC2 security group. Enable automated backups.
3. Create one small Amazon Linux EC2 instance. Allow 22 only from your IP and allow public 80/443. Do not expose 8080 publicly.
4. Install Java 21 and Nginx, create the `invoiceapp` system user, and create `/opt/invoice-verification` plus `/etc/invoice-verification`.
5. Copy `deploy/aws/invoice.env.example` to `/etc/invoice-verification/invoice.env`, insert secrets, and restrict it with `chmod 600`. Never commit the completed file.
6. Build with `./mvnw clean verify`, then `./mvnw -DskipTests package`. Copy `target/invoice-verification-system-0.0.1-SNAPSHOT.jar` to `/opt/invoice-verification/app.jar`.
7. Install `invoice-verification.service` under `/etc/systemd/system`, run `sudo systemctl daemon-reload`, then `sudo systemctl enable --now invoice-verification`.
8. Verify `curl -H 'X-Forwarded-Proto: https' http://127.0.0.1:8080/api/system/health/ready` returns `UP`.
9. After the first admin is created, remove `ADMIN_PASSWORD` and the other seed-admin values from the environment file, set `SEED_ADMINS=false`, and restart.
10. Point your domain to the EC2 address, obtain a Let's Encrypt certificate, install the Nginx example, and verify HTTPS. The production profile redirects HTTP application requests to HTTPS.
11. Test Aigner registration, admin login, cross-brand denial, document download and status audit before sharing the URL.

For each frontend or backend update, run the tests, build a new JAR, copy it to the server using a temporary filename, replace `app.jar`, and restart the service. Keep the previous JAR as a rollback version until the health check passes.

AWS credits are temporary. Create a billing budget and alerts before provisioning RDS or EC2. A custom domain is normally a separate paid annual purchase.
