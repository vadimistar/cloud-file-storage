package com.vadimistar.cloudfilestorage.config;

import com.vadimistar.cloudfilestorage.services.BucketService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MinioBucketCreator {

    private final MinioConfig minioConfig;

    private final BucketService bucketService;

    @PostConstruct
    @SneakyThrows
    public void createBucketIfNotExists() {
        if (!bucketService.isBucketExists(minioConfig.getBucketName())) {
            bucketService.createBucket(minioConfig.getBucketName());
        }
    }
}
