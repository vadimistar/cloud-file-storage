package com.vadimistar.cloudfilestorage.common;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;

@UtilityClass
public class TestUtils {

    public static MinIOContainer createMinIOContainer() {
        return new MinIOContainer("minio/minio:RELEASE.2024-02-26T09-33-48Z.fips");
    }

    public static void addMinioProperties(DynamicPropertyRegistry registry, MinIOContainer minIOContainer) {
        registry.add("minio.endpoint", minIOContainer::getS3URL);
        registry.add("minio.access-key", minIOContainer::getUserName);
        registry.add("minio.secret-key", minIOContainer::getPassword);
        registry.add("minio.bucket-name", () -> MINIO_BUCKET_NAME);
    }

    public static MySQLContainer<?> createMySqlContainer() {
        return new MySQLContainer<>("mysql:8.0");
    }

    public static void addMySqlProperties(DynamicPropertyRegistry registry, MySQLContainer<?> mysql) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    private static final String MINIO_BUCKET_NAME = "user-files-test";

}
