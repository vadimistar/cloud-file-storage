package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.exceptions.FileAlreadyExistsException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.utils.Minio;
import com.vadimistar.cloudfilestorage.utils.MinioUtils;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import io.minio.*;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileServiceImpl extends MinioService implements FileService {

    public FileServiceImpl(Minio minio) {
        super(minio);
    }

    @Override
    public void uploadFile(long userId, InputStream inputStream, long objectSize, String path){
        validateResourceNotExists(userId, path);
        minio.putObject(MinioUtils.getMinioPath(userId, path), inputStream, objectSize);
    }

    @Override
    public String renameFile(long userId, String path, String name){
        String newPath = PathUtils.join(PathUtils.getParentDirectory(path), name);
        validateResourceNotExists(userId, newPath);
        minio.copyObject(MinioUtils.getMinioPath(userId, path), MinioUtils.getMinioPath(userId, newPath));
        deleteFile(userId, path);
        return newPath;
    }

    @Override
    public void deleteFile(long userId, String path){
        minio.removeObject(MinioUtils.getMinioPath(userId, path));
    }

    @Override
    public byte[] downloadFile(long userId, String path){
        try (GetObjectResponse response = minio.getObject(MinioUtils.getMinioPath(userId, path))) {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
