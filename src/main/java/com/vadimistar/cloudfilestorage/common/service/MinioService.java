package com.vadimistar.cloudfilestorage.common.service;

import com.vadimistar.cloudfilestorage.common.exceptions.FileAlreadyExistsException;
import com.vadimistar.cloudfilestorage.common.exceptions.FolderAlreadyExistsException;
import com.vadimistar.cloudfilestorage.adapters.minio.ListObjectsMode;
import com.vadimistar.cloudfilestorage.adapters.minio.Minio;
import com.vadimistar.cloudfilestorage.common.util.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MinioService {

    protected final Minio minio;

    public boolean isFileExists(long userId, String path) {
        return minio.statObject(MinioUtils.getMinioPath(userId, path)).isPresent();
    }

    public boolean isFolderExists(long userId, String path) {
        path = PathUtils.makeDirectoryPath(path);
        String prefix = MinioUtils.getMinioPath(userId, path);
        return minio.listObjects(prefix, ListObjectsMode.NON_RECURSIVE).findAny().isPresent();
    }

    public void validateResourceNotExists(long userId, String path) {
        if (isFileExists(userId, path)) {
            throw new FileAlreadyExistsException("File already exists: " + path, PathUtils.getParentDirectory(path));
        }
        if (isFolderExists(userId, path)) {
            throw new FolderAlreadyExistsException("Folder already exists: " + path, PathUtils.getParentDirectory(path));
        }
    }
}
