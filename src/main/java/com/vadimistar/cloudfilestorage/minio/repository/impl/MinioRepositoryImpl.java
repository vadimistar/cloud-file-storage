package com.vadimistar.cloudfilestorage.minio.repository.impl;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.minio.exception.MinioException;
import com.vadimistar.cloudfilestorage.minio.config.MinioConfig;
import com.vadimistar.cloudfilestorage.minio.mapper.ListObjectsResponseDtoMapper;
import com.vadimistar.cloudfilestorage.common.util.StreamUtils;
import com.vadimistar.cloudfilestorage.minio.repository.MinioRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class MinioRepositoryImpl implements MinioRepository {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public Stream<ListObjectsResponseDto> listObjects(String prefix, boolean recursive) {
        Iterable<Result<Item>> items = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .prefix(prefix)
                .recursive(recursive)
                .build());
        return StreamUtils.asStream(items).map(itemResult -> {
            try {
                return itemResult.get();
            } catch (Exception e) {
                throw new MinioException(e.getMessage());
            }
        }).map(ListObjectsResponseDtoMapper::makeListObjectsResponseDto);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void makeBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    @Override
    public boolean isBucketExists() {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    @Override
    public boolean isObjectExists(String object) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(object)
                    .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    @Override
    public void removeObjects(String prefix) {
        Iterator<DeleteObject> objects = listObjects(prefix, true)
                .map(item -> new DeleteObject(item.getName()))
                .iterator();
        minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .objects(() -> objects)
                .build()
        ).forEach(MinioRepositoryImpl::handleRemoveObjectResult);
    }

    @Override
    public void removeBucket() {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    private static void handleRemoveObjectResult(Result<DeleteError> result) {
        try {
            DeleteError deleteError = result.get();
            throw new MinioException(deleteError.message());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    private static final long FILE_PART_SIZE = -1;
}

