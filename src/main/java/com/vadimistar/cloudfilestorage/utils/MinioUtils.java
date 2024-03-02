package com.vadimistar.cloudfilestorage.utils;

import io.minio.messages.Item;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MinioUtils {

    public static String getMinioPath(long userId, String path) {
        return PathUtils.join(PATH_FOR_USER.formatted(userId), path);
    }

    public static String getNormalPath(String minioPath) {
        return PathUtils.trimIndexDirectory(minioPath);
    }

    public static boolean isDirectory(Item item) {
        return item.isDir() || item.size() == 0;
    }

    private static final String PATH_FOR_USER = "user-%d-files/";
}
