package com.vadimistar.cloudfilestorage.utils;

import com.vadimistar.cloudfilestorage.dto.BreadcrumbElementDto;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BreadcrumbParser {

    public static List<BreadcrumbElementDto> parseBreadcrumb(String path) {
        List<BreadcrumbElementDto> result = new ArrayList<>();

        BreadcrumbElementDto homeDirectory = new BreadcrumbElementDto("/", "/");
        result.add(homeDirectory);

        String[] pathParts = path.split("/");

        StringBuilder pathBuilder = new StringBuilder("/?path=");
        for (int i = 0; i < pathParts.length; i ++) {
            pathBuilder.append(URLUtils.encode(pathParts[i]));
            result.add(new BreadcrumbElementDto(pathParts[i], pathBuilder.toString()));
            pathBuilder.append(URLUtils.encode("/"));
        }

        return result;
    }
}
