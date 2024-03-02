package com.vadimistar.cloudfilestorage.utils;

import com.vadimistar.cloudfilestorage.config.MinioConfig;
import com.vadimistar.cloudfilestorage.exceptions.MinioException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class Minio {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public Stream<Item> listObjects(String prefix, ListObjectsMode mode) {
        Iterable<Result<Item>> items = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .prefix(prefix)
                .recursive(mode.isRecursive())
                .build());
        return StreamUtils.asStream(items).map(itemResult -> {
            try {
                return itemResult.get();
            } catch (Exception e) {
                throw new MinioException(e.getMessage());
            }
        });
    }

    public void copyObject(String from, String to) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(to)
                    .source(
                            CopySource.builder()
                                    .bucket(minioConfig.getBucketName())
                                    .object(from)
                                    .build()
                    )
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void removeObject(String object) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(object)
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void putObject(String object, InputStream stream, long objectSize) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(object)
                    .stream(stream, objectSize, FILE_PART_SIZE)
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public GetObjectResponse getObject(String object) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(object)
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void makeBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public boolean isBucketExists() {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public Optional<StatObjectResponse> statObject(String object) {
        try {
            return Optional.of(minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(object)
                    .build()
            ));
        } catch (ErrorResponseException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    private static final long FILE_PART_SIZE = -1;
}

