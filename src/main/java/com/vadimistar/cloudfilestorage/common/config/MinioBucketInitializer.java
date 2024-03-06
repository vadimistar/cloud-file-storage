package com.vadimistar.cloudfilestorage.common.config;

import com.vadimistar.cloudfilestorage.common.repository.MinioRepository;
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
