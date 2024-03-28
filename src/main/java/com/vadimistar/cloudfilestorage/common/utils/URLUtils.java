package com.vadimistar.cloudfilestorage.common.utils;

import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class URLUtils {

    public static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
