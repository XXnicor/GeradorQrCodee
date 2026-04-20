package com.GeradorQrCode.qrcode.infrastructure;

import com.GeradorQrCode.qrcode.ports.StoredFileResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.GeradorQrCode.qrcode.ports.StoragePort;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Component
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;
    private final Duration presignedDuration;

    public S3StorageAdapter(@Value("${aws.s3.bucket}") String bucketName, 
                            @Value("${aws.s3.region}") String region,
                            @Value("${aws.s3.presigned-duration-minutes:15}") long presignedDurationMinutes) {
        this.bucketName = bucketName;
        this.presignedDuration = Duration.ofMinutes(presignedDurationMinutes);

        Region awsRegion = Region.of(region);
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public StoredFileResult uploadFile(byte[] fileData, String fileName, String contentType) {
       PutObjectRequest putObjectRequest = PutObjectRequest.builder()
               .bucket(bucketName)
               .key(fileName)
               .contentType(contentType)
               .build();

         s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

         GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                 .bucket(bucketName)
                 .key(fileName)
                 .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(request -> request
                .signatureDuration(presignedDuration)
                .getObjectRequest(getObjectRequest));

        return new StoredFileResult(presignedGetObjectRequest.url().toString(), presignedDuration.getSeconds(), fileName, contentType);

    }
}
