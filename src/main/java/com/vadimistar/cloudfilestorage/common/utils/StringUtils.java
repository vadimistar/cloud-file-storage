package com.vadimistar.cloudfilestorage.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static String removePrefix(String str, String prefix) {
        if (str.startsWith(prefix)) {
            return str.replaceFirst(prefix, "");
        }
        return str;
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

    public static long count(String s, char c) {
        return s.chars().filter(ch -> ch == c).count();
    }
}
