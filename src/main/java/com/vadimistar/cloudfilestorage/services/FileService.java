package com.vadimistar.cloudfilestorage.services;

import java.io.InputStream;

public interface FileService {

    void uploadFile(long userId, InputStream inputStream, long objectSize, String path);

    String renameFile(long userId, String oldPath, String name);

    void deleteFile(long userId, String path);

    byte[] downloadFile(long userId, String path);

    boolean isFileExists(long userId, String path);
}
