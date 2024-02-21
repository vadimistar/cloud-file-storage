package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface FileService {

    void createFolder(long userId, String path) throws FileServiceException;

    void uploadFile(long userId, InputStream inputStream, long objectSize, String path) throws FileServiceException;

    void renameFile(long userId, String oldPath, String newPath) throws FileServiceException;

    void deleteFile(long userId, String path) throws FileServiceException;

    void deleteFolder(long userId, String path) throws FileServiceException;

    List<FileDto> getFilesInFolder(long userId, String path) throws FileServiceException;

    boolean isFileExists(long userId, String path) throws FileServiceException;

    Optional<InputStream> downloadFile(long userId, String path) throws FileServiceException;

    boolean isFolderExists(long userId, String path) throws FileServiceException;
}
