package com.vadimistar.cloudfilestorage.index.breadcrumbs.service.impl;

import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.dto.BreadcrumbsElementDto;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.service.BreadcrumbsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BreadcrumbsServiceImpl implements BreadcrumbsService {

    public List<BreadcrumbsElementDto> createBreadcrumbs(String path) {
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
