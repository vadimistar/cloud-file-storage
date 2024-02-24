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
}
