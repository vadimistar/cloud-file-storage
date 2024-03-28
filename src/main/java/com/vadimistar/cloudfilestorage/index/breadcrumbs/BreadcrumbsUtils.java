package com.vadimistar.cloudfilestorage.index.breadcrumbs;

import com.vadimistar.cloudfilestorage.common.utils.URLUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BreadcrumbsUtils {

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
