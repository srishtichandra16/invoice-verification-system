package com.invoiceverification.invoice_verification_system.invoice.service;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.util.UUID;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class S3Service {
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    public String uploadInvoice(MultipartFile file, Brand brand) {

        String extension = switch (file.getContentType() == null ? "" : file.getContentType()) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".bin";
        };
        String fileName = UUID.randomUUID() + extension;

        String objectKey =
                "brands/" + brand.getSlug() + "/invoices/" + fileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return objectKey;

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to upload invoice to S3",
                    exception
            );
        }
    }

    public String createDownloadUrl(Invoice invoice) {
        int extensionStart = invoice.getObjectKey().lastIndexOf('.');
        String extension = extensionStart >= 0 ? invoice.getObjectKey().substring(extensionStart) : "";
        GetObjectRequest get = GetObjectRequest.builder().bucket(bucketName).key(invoice.getObjectKey())
                .responseContentDisposition("attachment; filename=\"invoice-document" + extension + "\"")
                .responseContentType("application/octet-stream").build();
        GetObjectPresignRequest request = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(5)).getObjectRequest(get).build();
        return presigner.presignGetObject(request).url().toString();
    }

    public void deleteInvoiceQuietly(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (RuntimeException exception) {
            log.error("Could not remove a rolled-back S3 object", exception);
        }
    }
}
