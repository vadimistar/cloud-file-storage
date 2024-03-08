package com.vadimistar.cloudfilestorage.minio.utils;

import com.vadimistar.cloudfilestorage.common.exception.ResourceAlreadyExistsException;
import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

@UtilityClass
public class MinioUtils {

    public static String getMinioPath(long userId, String path) {
        return PathUtils.join(PATH_FOR_USER.formatted(userId), path);
    }

    public static String getNormalPath(String minioPath) {
        return PathUtils.trimIndexDirectory(minioPath);
    }

    public static boolean isDirectory(ListObjectsResponseDto listObjectsResponseDto) {
        return listObjectsResponseDto.isDirectory() || listObjectsResponseDto.getSize() == 0;
    }

    public static void validateResourceNotExists(MinioService minioService, long userId, String path) {
        String object = getMinioPath(userId, path);
        if (minioService.isFileExists(object)) {
            throw new ResourceAlreadyExistsException("File already exists: " + path, path);
        } else if (minioService.isFolderExists(object)) {
            throw new ResourceAlreadyExistsException("Folder already exists: " + path, path);
        }
    }

    private static final String PATH_FOR_USER = "user-%d-files/";
}
