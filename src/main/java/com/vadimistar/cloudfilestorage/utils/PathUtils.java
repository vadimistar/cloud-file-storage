package com.vadimistar.cloudfilestorage.utils;

import lombok.experimental.UtilityClass;

import java.nio.file.Paths;
import java.util.Objects;

@UtilityClass
public class PathUtils {

    public static String getRelativePath(String path, String directory) {
        return StringUtils.removePrefix(path, directory);
    }

    public static String getParentDirectory(String path) {
        return Objects.requireNonNullElse(
                Paths.get(path).getParent(), ""
        ).toString().replace("\\", "/");
    }

    public static boolean isHomeDirectory(String path) {
        return path.isEmpty() || path.equals("/");
    }

    public static String getChildPath(String parent, String child) {
        if (isHomeDirectory(parent)) {
            return child;
        }
        return StringUtils.addSuffix(parent, "/") + child;
    }
}
