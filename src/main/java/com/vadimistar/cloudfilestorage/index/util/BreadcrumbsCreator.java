package com.vadimistar.cloudfilestorage.index.util;

import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.index.dto.BreadcrumbsElementDto;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BreadcrumbsCreator {

    public static List<BreadcrumbsElementDto> createBreadcrumbs(String path) {
        List<BreadcrumbsElementDto> result = new ArrayList<>();

        BreadcrumbsElementDto homeDirectory = new BreadcrumbsElementDto("/", "/");
        result.add(homeDirectory);

        StringBuilder pathBuilder = new StringBuilder("/?path=");

        for (String pathPart : path.split("/")) {
            pathBuilder.append(URLUtils.encode(pathPart));
            result.add(new BreadcrumbsElementDto(pathPart, pathBuilder.toString()));
            pathBuilder.append(URLUtils.encode("/"));
        }

        return result;
    }
}