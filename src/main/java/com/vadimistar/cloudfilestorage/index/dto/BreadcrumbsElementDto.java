package com.vadimistar.cloudfilestorage.index.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BreadcrumbsElementDto {

    private String name;

    private String path;
}