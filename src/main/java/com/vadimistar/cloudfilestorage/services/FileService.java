package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface FileService {

    void createFolder(long userId, String path) throws FileServiceException;

    void uploadFile(long userId, InputStream inputStream, long objectSize, String path) throws FileServiceException;

    String renameFile(long userId, String oldPath, String name) throws FileServiceException;

    String renameDirectory(long userId, String oldPath, String name) throws FileServiceException;

    void deleteFile(long userId, String path) throws FileServiceException;

    void deleteDirectory(long userId, String path) throws FileServiceException;

    List<FileDto> getFilesInDirectory(long userId, String path) throws FileServiceException;

    byte[] downloadDirectory(long userId, String path) throws FileServiceException;

    byte[] downloadFile(long userId, String path) throws FileServiceException;

    Optional<FileDto> statObject(long userId, String path) throws FileServiceException;

    boolean isDirectoryExists(long userId, String path) throws FileServiceException;
}
