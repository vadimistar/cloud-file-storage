package com.vadimistar.cloudfilestorage.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static String removePrefix(String str, String prefix) {
        return str.replaceFirst(prefix, "");
    }

    public static String addSuffix(String str, String suffix) {
        if (!str.endsWith(suffix)) {
            return str + suffix;
        }
        return str;
    }

    public static String removeSuffix(String str, String suffix) {
        int index = str.lastIndexOf(suffix);
        if (index > 0) {
            return str.substring(0, index);
        }
        return str;
    }
}
