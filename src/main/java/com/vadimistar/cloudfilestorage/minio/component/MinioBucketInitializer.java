package com.vadimistar.cloudfilestorage.minio.component;

import com.vadimistar.cloudfilestorage.minio.repository.MinioRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MinioBucketInitializer {

    private final MinioRepository minioRepository;

    @PostConstruct
    public void initBucketIfNotExists() {
        if (!minioRepository.isBucketExists()) {
            minioRepository.makeBucket();
        }
    }
}
