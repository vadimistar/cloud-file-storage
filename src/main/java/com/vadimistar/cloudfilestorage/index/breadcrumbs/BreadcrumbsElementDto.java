package com.vadimistar.cloudfilestorage.index.breadcrumbs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BreadcrumbsElementDto {

    private String name;
    private String path;
}
