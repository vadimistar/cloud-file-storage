package com.vadimistar.cloudfilestorage.common.util;

import com.vadimistar.cloudfilestorage.minio.exception.ResourceAlreadyExistsException;
import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MinioUtils {

    public static String getMinioPath(long userId, String path) {
        path = PathUtils.uriEncodePath(path);
        return PathUtils.join(PATH_FOR_USER.formatted(userId), path);
    }

    public static String getNormalPath(String minioPath) {
        minioPath = PathUtils.uriDecodePath(minioPath);
        return PathUtils.trimIndexDirectory(minioPath);
    }

    public static boolean isDirectory(ListObjectsResponseDto listObjectsResponseDto) {
        return listObjectsResponseDto.getSize() == 0 && listObjectsResponseDto.getName().endsWith("/");
    }

    public static void validateResourceNotExists(MinioService minioService, long userId, String path) {
        String object = getMinioPath(userId, path);
        if (minioService.isFileExists(object)) {
            throw new ResourceAlreadyExistsException("File already exists: " + path, path);
        } else if (minioService.isFolderExists(object)) {
            throw new ResourceAlreadyExistsException("Folder already exists: " + path, path);
        }
    }

    public static String getMinioFilename(String path) {
        return URLUtils.encode(PathUtils.getFilename(path));
    }

    public static String getNormalFilename(String path) {
        return URLUtils.decode(PathUtils.getFilename(path));
    }

    private static final String PATH_FOR_USER = "user-%d-files/";
}
