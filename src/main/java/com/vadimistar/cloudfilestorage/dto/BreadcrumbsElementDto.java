package com.vadimistar.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BreadcrumbsElementDto {

    private String name;

    private String path;
}
