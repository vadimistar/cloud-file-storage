package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.config.MinioConfig;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.services.BucketService;
import com.vadimistar.cloudfilestorage.services.FileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioConfig minioConfig;

    private final MinioClient minioClient;

    private final BucketService bucketService;

    private static final long FILE_PART_SIZE = -1;

    @Override
    public void createFolder(long userId, String path) throws FileServiceException {
        uploadFile(userId, new ByteArrayInputStream(new byte[] {}), 0, getFolderPath(path));
    }

    @Override
    public void uploadFile(long userId, InputStream inputStream, long objectSize, String path) throws FileServiceException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(getObjectPath(userId, path))
                    .stream(inputStream, objectSize, FILE_PART_SIZE)
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public void renameFile(long userId, String oldPath, String newPath) throws FileServiceException {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(getObjectPath(userId, newPath))
                    .source(
                            CopySource.builder()
                                    .bucket(minioConfig.getMinioBucketName())
                                    .object(getObjectPath(userId, oldPath))
                                    .build()
                    )
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new FileServiceException(e.getMessage());
        }

        deleteFile(userId, oldPath);
    }

    @Override
    public void deleteFile(long userId, String path) throws FileServiceException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(getObjectPath(userId, path))
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public void deleteFolder(long userId, String path) throws FileServiceException {
        deleteFile(userId, getFolderPath(path));
    }

    public List<FileDto> getFilesInFolder(long userId, String path) throws FileServiceException {
        String objectPath = getObjectPath(userId, getFolderPath(path));
        List<FileDto> files = new ArrayList<>();

        try {
            for (Result<Item> itemResult : minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .prefix(objectPath)
                    .build())) {
                Item item = itemResult.get();
                files.add(FileDto.builder()
                        .name(getFileName(item.objectName()))
                        .isDirectory(item.isDir())
                        .path(URLEncoder.encode(getFilePath(item.objectName()), StandardCharsets.UTF_8))
                        .build());
            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            // TODO: maybe change to one type (Exception)?

            throw new FileServiceException(e.getMessage());
        }

        return files;
    }

    @Override
    public boolean isFileExists(long userId, String path) throws FileServiceException {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(getObjectPath(userId, path))
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public Optional<InputStream> downloadFile(long userId, String path) throws FileServiceException {
        String objectPath = getObjectPath(userId, path);

        try {
            return Optional.of(minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(objectPath)
                    .build()));
        } catch (ErrorResponseException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public boolean isFolderExists(long userId, String path) throws FileServiceException {
        if (path.isEmpty()) { return true; }
        String objectPath = getObjectPath(userId, getFolderPath(path));

        try {
            Iterator<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .prefix(objectPath)
                    .build()).iterator();
            return objects.hasNext();
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    private static final String USER_PATH_FORMAT = "user-%d-files/%s";

    private static String getObjectPath(long userId, String path) {
        return USER_PATH_FORMAT.formatted(userId, path);
    }

    private static String getFolderPath(String path) {
        if (path.isEmpty()) { return path; }
        if (!path.endsWith("/")) { return path + "/"; }
        return path;
    }

    private static String getFileName(String path) {
        String[] pathParts = path.split("/");
        return pathParts[pathParts.length - 1];
    }

    private static String getFilePath(String path) {
        String[] pathParts = path.split("/", 2);
        if (pathParts.length > 1) {
            return pathParts[1];
        }
        return pathParts[0];
    }
}
