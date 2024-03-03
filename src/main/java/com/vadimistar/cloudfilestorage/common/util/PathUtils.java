package com.vadimistar.cloudfilestorage.common.util;

import lombok.experimental.UtilityClass;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    public static String join(String parent, String child) {
        if (isHomeDirectory(parent)) {
            return child;
        }
        return StringUtils.addSuffix(parent, "/") + StringUtils.removePrefix(child, "/");
    }

    public static String getCurrentDirectoryName(String path) {
        String[] pathParts = path.split("/");
        if (pathParts.length == 0) {
            return path;
        }
        return pathParts[pathParts.length - 1];
    }

    public static List<String> getSubdirectories(String path) {
        List<String> result = new ArrayList<>();
        String[] pathParts = StringUtils.removeSuffix(path, "/").split("/");
        StringBuilder sb = new StringBuilder();
        for (String part : pathParts) {
            sb.append(part);
            sb.append('/');
            result.add(sb.toString());
        }
        return result;
    }

    public static String makeDirectoryPath(String path) {
        return StringUtils.addSuffix(path, "/");
    }

    public static String getFilename(String path) {
        String[] pathParts = path.split("/");
        return StringUtils.removeSuffix(pathParts[pathParts.length - 1], "/");
    }

    public static String trimIndexDirectory(String path) {
        String[] pathParts = path.split("/", 2);
        if (pathParts.length > 1) {
            return pathParts[1];
        }
        return pathParts[0];
    }
}