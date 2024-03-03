package com.vadimistar.cloudfilestorage.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {

    private String name;

    private boolean isFolder;

    private String path;
}
