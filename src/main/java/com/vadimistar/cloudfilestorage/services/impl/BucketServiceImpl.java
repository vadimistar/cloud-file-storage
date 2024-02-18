package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.exceptions.BucketServiceException;
import com.vadimistar.cloudfilestorage.services.BucketService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@AllArgsConstructor
public class BucketServiceImpl implements BucketService {

    private final MinioClient minioClient;

    @Override
    public void createBucket(String name) throws BucketServiceException {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(name)
                    .build());
        } catch (ErrorResponseException | XmlParserException | NoSuchAlgorithmException | IOException |
                 InvalidResponseException | ServerException | InvalidKeyException | InternalException |
                 InsufficientDataException e) {
            throw new BucketServiceException(e.getMessage());
        }
    }

    @Override
    public boolean isBucketExists(String name) throws BucketServiceException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(name)
                        .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new BucketServiceException(e.getMessage());
        }
    }
}
