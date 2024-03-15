package com.vadimistar.cloudfilestorage.folder.service;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

public interface FolderService {

    void createFolder(long userId, String path);

    void uploadFolder(long userId, MultipartFile[] files, String path);

    String renameFolder(long userId, String path, String name);

    void deleteFolder(long userId, String path);

    Stream<FileDto> getFolderContent(long userId, String path);

    Stream<FileDto> getAllContent(long userId);

    byte[] downloadFolder(long userId, String path);

    boolean isFolderExists(long userId, String path);

    default void createUnnamedFolder(long userId, String path, int maxAttempts) {
        for (int attempts = 1; attempts <= maxAttempts; attempts ++) {
            String resultPath = PathUtils.join(path, "New Folder (%d)".formatted(attempts));
            if (!isFolderExists(userId, resultPath)) {
                createFolder(userId, resultPath);
                break;
            }
        }
    }
}
