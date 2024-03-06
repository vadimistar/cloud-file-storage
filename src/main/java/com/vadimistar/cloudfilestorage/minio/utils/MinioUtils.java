package com.vadimistar.cloudfilestorage.minio.utils;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import lombok.experimental.UtilityClass;

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

    private static final String PATH_FOR_USER = "user-%d-files/";
}
