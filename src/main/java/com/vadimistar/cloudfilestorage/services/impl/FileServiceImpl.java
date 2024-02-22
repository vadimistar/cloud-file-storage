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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.vadimistar.cloudfilestorage.utils.PathUtils.getRelativePath;

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

    @Override
    public List<FileDto> getFilesInFolder(long userId, String path) throws FileServiceException {
        return listFiles(userId, path, false);
    }

    @Override
    public byte[] downloadFolder(long userId, String path) throws FileServiceException {
        List<FileDto> files = listFiles(userId, path, true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (FileDto fileDto : files) {
                if (fileDto.isDirectory()) {
                    continue;
                }

                String relativePath = getRelativePath(fileDto.getPath(), path);

                ByteArrayResource byteArrayResource = new ByteArrayResource(
                        downloadFile(userId, fileDto.getPath())
                );

                ZipEntry zipEntry = new ZipEntry(relativePath);
                zipEntry.setSize(byteArrayResource.contentLength());
                zipEntry.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(zipEntry);
                StreamUtils.copy(byteArrayResource.getInputStream(), zipOutputStream);
                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();
        } catch (IOException e) {
            throw new FileServiceException(e.getMessage());
        }

        return byteArrayOutputStream.toByteArray();
    }

    private List<FileDto> listFiles(long userId, String path, boolean recursive) throws FileServiceException {
        String objectPath = getObjectPath(userId, getFolderPath(path));
        List<FileDto> files = new ArrayList<>();

        try {
            for (Result<Item> itemResult : minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .prefix(objectPath)
                    .recursive(recursive)
                    .build())) {
                Item item = itemResult.get();

                if (objectPath.equals(item.objectName())) {
                    continue;
                }

                files.add(FileDto.builder()
                        .name(getFileName(item.objectName()))
                        .isDirectory(item.isDir())
                        .path(getFilePath(item.objectName()))
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
    public byte[] downloadFile(long userId, String path) throws FileServiceException {
        String objectPath = getObjectPath(userId, path);

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(minioConfig.getMinioBucketName())
                .object(objectPath)
                .build();

        try (GetObjectResponse object = minioClient.getObject(getObjectArgs)) {
            return object.readAllBytes();
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

    @Override
    public Optional<FileDto> statFile(long userId, String path) throws FileServiceException {
        String[] partParts = path.split("/");
        if (partParts.length == 0) {
            return Optional.empty();
        }
        String filename = partParts[partParts.length - 1];

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(getObjectPath(userId, path))
                    .build());

            return Optional.of(FileDto.builder()
                    .isDirectory(path.endsWith("/"))
                    .name(filename)
                    .path(path)
                    .build());

        } catch (ErrorResponseException e) {
            Iterator<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .prefix(getObjectPath(userId, getFolderPath(path)))
                    .build()).iterator();

            if (objects.hasNext()) {
                return Optional.of(FileDto.builder()
                        .isDirectory(true)
                        .name(filename)
                        .path(path)
                        .build());
            }

            return Optional.empty();

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
