package com.vadimistar.cloudfilestorage.file.service.impl;

import com.vadimistar.cloudfilestorage.file.exception.FileNotFoundException;
import com.vadimistar.cloudfilestorage.file.service.FileService;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.minio.utils.MinioUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioService minioService;

    @Override
    public void uploadFile(long userId, InputStream inputStream, long objectSize, String path){
        MinioUtils.validateResourceNotExists(minioService, userId, path);
        minioService.putObject(MinioUtils.getMinioPath(userId, path), inputStream, objectSize);
    }

    @Override
    public String renameFile(long userId, String path, String name){
        validateFileExists(userId, path);
        String newPath = PathUtils.join(PathUtils.getParentDirectory(path), name);
        if (newPath.equals(path)) {
            return path;
        }
        MinioUtils.validateResourceNotExists(minioService, userId, newPath);
        minioService.copyObject(
                MinioUtils.getMinioPath(userId, path),
                MinioUtils.getMinioPath(userId, newPath)
        );
        deleteFile(userId, path);
        return newPath;
    }

    @Override
    public void deleteFile(long userId, String path) {
        validateFileExists(userId, path);
        minioService.removeObject(MinioUtils.getMinioPath(userId, path));
    }

    @Override
    public byte[] downloadFile(long userId, String path){
        validateFileExists(userId, path);
        try (InputStream inputStream = minioService.getObject(MinioUtils.getMinioPath(userId, path))) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFileExists(long userId, String path) {
        return minioService.isFileExists(MinioUtils.getMinioPath(userId, path));
    }

    private void validateFileExists(long userId, String path) {
        if (!isFileExists(userId, path)) {
            throw new FileNotFoundException("File is not found: " + path);
        }
    }
}
