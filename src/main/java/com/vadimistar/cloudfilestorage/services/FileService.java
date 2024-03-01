package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.CreateFolderRequestDto;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface FileService {

    void createNamedFolder(long userId, String path) throws FileServiceException;

    void uploadFile(long userId, InputStream inputStream, long objectSize, String path) throws FileServiceException;

    void uploadFolder(long userId, MultipartFile[] files, String path) throws FileServiceException, IOException;

    String renameFile(long userId, String oldPath, String name) throws FileServiceException;

    String renameDirectory(long userId, String oldPath, String name) throws FileServiceException;

    void deleteFile(long userId, String path) throws FileServiceException;

    void deleteDirectory(long userId, String path) throws FileServiceException;

    List<FileDto> getFilesInDirectory(long userId, String path) throws FileServiceException;

    byte[] downloadDirectory(long userId, String path) throws FileServiceException;

    byte[] downloadFile(long userId, String path) throws FileServiceException;

    Optional<FileDto> statObject(long userId, String path) throws FileServiceException;

    boolean isDirectoryExists(long userId, String path) throws FileServiceException;

    List<FileDto> getAllFiles(long userId) throws FileServiceException;

    default void createUnnamedFolder(long userId, String path, int maxAttempts) throws FileServiceException {
        for (int attempts = 1; attempts <= maxAttempts; attempts ++) {
            String resultPath = PathUtils.getChildPath(path, "New Folder (%d)".formatted(attempts));
            if (!isDirectoryExists(userId, resultPath)) {
                createNamedFolder(userId, resultPath);
                break;
            }
        }
    }
}
