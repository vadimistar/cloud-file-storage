package com.vadimistar.cloudfilestorage.index.breadcrumbs.service;

import com.vadimistar.cloudfilestorage.index.breadcrumbs.dto.BreadcrumbsElementDto;

import java.util.List;

public interface BreadcrumbsService {
    List<BreadcrumbsElementDto> createBreadcrumbs(String path);
}
