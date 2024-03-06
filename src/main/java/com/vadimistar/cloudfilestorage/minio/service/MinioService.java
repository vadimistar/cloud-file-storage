package com.vadimistar.cloudfilestorage.minio.service;

import com.vadimistar.cloudfilestorage.common.exceptions.FileAlreadyExistsException;
import com.vadimistar.cloudfilestorage.common.exceptions.FolderAlreadyExistsException;
import com.vadimistar.cloudfilestorage.minio.repository.ListObjectsMode;
import com.vadimistar.cloudfilestorage.minio.repository.MinioRepository;
import com.vadimistar.cloudfilestorage.minio.utils.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class MinioService {

    protected final MinioRepository minioRepository;

    public boolean isFileExists(long userId, String path) {
        String object = MinioUtils.getMinioPath(userId, path);
        return minioRepository.isObjectExists(object);
    }

    public boolean isFolderExists(long userId, String path) {
        path = PathUtils.makeDirectoryPath(path);
        String prefix = MinioUtils.getMinioPath(userId, path);
        return minioRepository.listObjects(prefix, ListObjectsMode.NON_RECURSIVE).findAny().isPresent();
    }

    public void validateResourceNotExists(long userId, String path) {
        if (isFileExists(userId, path)) {
            throw new FileAlreadyExistsException("File already exists: " + path, path);
        }
        if (isFolderExists(userId, path)) {
            throw new FolderAlreadyExistsException("Folder already exists: " + path, path);
        }
    }
}
