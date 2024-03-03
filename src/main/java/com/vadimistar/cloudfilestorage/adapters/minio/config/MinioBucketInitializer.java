package com.vadimistar.cloudfilestorage.adapters.minio.config;

import com.vadimistar.cloudfilestorage.adapters.minio.Minio;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MinioBucketInitializer {

    private final Minio minio;

    @PostConstruct
    public void initBucketIfNotExists() {
        if (!minio.isBucketExists()) {
            minio.makeBucket();
        }
    }
}
