package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.config.MinioConfig;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.services.FileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.vadimistar.cloudfilestorage.utils.PathUtils.getParentDirectory;
import static com.vadimistar.cloudfilestorage.utils.PathUtils.getRelativePath;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioConfig minioConfig;

    private final MinioClient minioClient;

    private static final long FILE_PART_SIZE = -1;

    @Override
    public void createFolder(long userId, String path) throws FileServiceException {
        uploadFile(userId, new ByteArrayInputStream(new byte[] {}), 0, getFolderPath(path));
    }

    @Override
    public void uploadFile(long userId, InputStream inputStream, long objectSize, String path) throws FileServiceException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(getObjectPath(userId, path))
                    .stream(inputStream, objectSize, FILE_PART_SIZE)
                    .build());
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public String renameFile(long userId, String oldPath, String name) throws FileServiceException {
        String newPath = getParentDirectory(oldPath) + "/" + name;

        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(getObjectPath(userId, newPath))
                    .source(
                            CopySource.builder()
                                    .bucket(minioConfig.getBucketName())
                                    .object(getObjectPath(userId, oldPath))
                                    .build()
                    )
                    .build());
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }

        deleteFile(userId, oldPath);

        return newPath;
    }

    @Override
    public String renameDirectory(long userId, String oldPath, String name) throws FileServiceException {
        List<String> filesToRename = new ArrayList<>();
        List<String> directoriesToRename = new ArrayList<>();
        String newPath = getFolderPath(getParentDirectory(oldPath) + "/" + name);

        try {
            for (Result<Item> itemResult : minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .prefix(getObjectPath(userId, oldPath))
                    .recursive(true)
                    .build())) {
                Item item = itemResult.get();

                if (item.isDir() || item.size() == 0) {
                    directoriesToRename.add(item.objectName());
                } else {
                    filesToRename.add(item.objectName());
                }
            }

            for (String file : filesToRename) {
                minioClient.copyObject(CopyObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(file.replaceFirst(oldPath, newPath))
                        .source(
                                CopySource.builder()
                                        .bucket(minioConfig.getBucketName())
                                        .object(file)
                                        .build()
                        )
                        .build());

                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(file)
                        .build());
            }

            for (String directory : directoriesToRename) {
                createEmptyObject(directory);

                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(directory)
                        .build());
            }

        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }

        return newPath;
    }

    @Override
    public void deleteFile(long userId, String path) throws FileServiceException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(getObjectPath(userId, path))
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public void deleteDirectory(long userId, String path) throws FileServiceException {
        try {
            Iterable<Result<Item>> items = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .prefix(getObjectPath(userId, getFolderPath(path)))
                    .recursive(true)
                    .build());

            for (Result<Item> item : items) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(item.get().objectName())
                        .build());
            }
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public List<FileDto> getFilesInDirectory(long userId, String path) throws FileServiceException {
        return listFiles(userId, path, false);
    }

    @Override
    public byte[] downloadFile(long userId, String path) throws FileServiceException {
        String objectPath = getObjectPath(userId, path);

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectPath)
                .build();

        try (GetObjectResponse object = minioClient.getObject(getObjectArgs)) {
            return object.readAllBytes();
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }
    }

    @Override
    public byte[] downloadDirectory(long userId, String path) throws FileServiceException {
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

    @Override
    public boolean isDirectoryExists(long userId, String path) throws FileServiceException {
        Iterator<Result<Item>> itemResult = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .prefix(getFolderPath(getObjectPath(userId, path)))
                .recursive(false)
                .build()).iterator();

        return itemResult.hasNext();
    }

    private List<FileDto> listFiles(long userId, String path, boolean recursive) throws FileServiceException {
        String objectPath = getObjectPath(userId, getFolderPath(path));
        List<FileDto> files = new ArrayList<>();

        try {
            for (Result<Item> itemResult : minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
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
        } catch (Exception e) {
            throw new FileServiceException(e.getMessage());
        }

        return files;
    }

    @Override
    public Optional<FileDto> statObject(long userId, String path) throws FileServiceException {
        String[] partParts = path.split("/");
        if (partParts.length == 0) {
            return Optional.empty();
        }
        String filename = partParts[partParts.length - 1];

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(getObjectPath(userId, path))
                    .build());

            return Optional.of(FileDto.builder()
                    .isDirectory(path.endsWith("/"))
                    .name(filename)
                    .path(path)
                    .build());

        } catch (ErrorResponseException e) {
            Iterator<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
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

    private void createEmptyObject(String objectName) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .stream(new ByteArrayInputStream(new byte[] {}), 0, FILE_PART_SIZE)
                .build());
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
