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

        StringBuilder pathBuilder = new StringBuilder("/?path=");

        for (String pathPart : path.split("/")) {
            pathBuilder.append(pathPart);
            result.add(new BreadcrumbElementDto(pathPart, pathBuilder.toString()));
            pathBuilder.append("/");
        }

        return result;
    }
}
