package com.vadimistar.cloudfilestorage.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtils {

    public static String getFilename(String path) {
        String[] pathParts = path.split("/");
        return pathParts[pathParts.length - 1];
    }
}
