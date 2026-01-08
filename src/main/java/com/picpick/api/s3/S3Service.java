package com.picpick.api.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket:picpick-bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String key = folder + "/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucketName, key, inputStream);
            log.info("Uploaded file to S3: {}", key);
            return key;
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public void deleteFile(String key) {
        try {
            s3Template.deleteObject(bucketName, key);
            log.info("Deleted file from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }
}
