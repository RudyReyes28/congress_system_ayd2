package com.alessandro.congress_management.services.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.alessandro.congress_management.exceptions.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


@Service
public class S3FileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final AmazonS3 amazonS3;
    private final String bucketName;
    private final String region;

    public S3FileStorageService(
            AmazonS3 amazonS3,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.region}") String region
    ) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        this.region = region;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) throws FileStorageException {
        validateFile(file);

        String key = buildObjectKey(folder, file.getOriginalFilename());

        try {
            ObjectMetadata metadata = buildMetadata(file);
            PutObjectRequest request = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);

            amazonS3.putObject(request);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read file input stream: " + file.getOriginalFilename());
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file to S3: " + file.getOriginalFilename());
        }

        return buildPublicUrl(key);
    }

    @Override
    public void deleteFile(String fileUrl) throws FileStorageException {
        if (fileUrl == null || fileUrl.isBlank()) return;

        String key = extractKeyFromUrl(fileUrl);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file from S3: " + fileUrl);
        }
    }

    private void validateFile(MultipartFile file) throws FileStorageException {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File must not be null or empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File size exceeds the 10 MB limit.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new FileStorageException(
                    "Invalid file type: " + file.getContentType() + ". Only PDF and Word documents are allowed."
            );
        }
    }

    private String buildObjectKey(String folder, String originalFilename) {
        String sanitized = sanitizeFilename(originalFilename);
        return folder + "/" + UUID.randomUUID() + "-" + sanitized;
    }

    private ObjectMetadata buildMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        return metadata;
    }

    private String buildPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private String extractKeyFromUrl(String fileUrl) throws FileStorageException {
        // URL format: https://{bucket}.s3.{region}.amazonaws.com/{key}
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        throw new FileStorageException("Cannot extract S3 key from URL: " + fileUrl);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}