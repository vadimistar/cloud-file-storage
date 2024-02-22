package com.vadimistar.cloudfilestorage.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtils {

    public static String getRelativePath(String path, String directory) {
        return StringUtils.removePrefix(path, directory);
    }
}
