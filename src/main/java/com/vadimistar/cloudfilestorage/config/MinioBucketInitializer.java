package com.vadimistar.cloudfilestorage.config;

import com.vadimistar.cloudfilestorage.utils.Minio;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
