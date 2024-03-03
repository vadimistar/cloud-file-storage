package com.vadimistar.cloudfilestorage.common;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MinIOContainer;

@UtilityClass
public class MinioTestUnits {

    public static MinIOContainer createMinIOContainer() {
        return new MinIOContainer("minio/minio:latest");
    }

    public static void addMinioProperties(DynamicPropertyRegistry registry, MinIOContainer minIOContainer) {
        registry.add("minio.endpoint", minIOContainer::getS3URL);
        registry.add("minio.access-key", minIOContainer::getUserName);
        registry.add("minio.secret-key", minIOContainer::getPassword);
        registry.add("minio.bucket-name", () -> MINIO_BUCKET_NAME);
    }

    private static final String MINIO_BUCKET_NAME = "user-files-test";

}
