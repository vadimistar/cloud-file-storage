package com.vadimistar.cloudfilestorage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FoundFileDto {

    private String name;

    private boolean isFolder;

    public String parentPath;
}
