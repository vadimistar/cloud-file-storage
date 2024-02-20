package com.vadimistar.cloudfilestorage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {

    private String name;

    private boolean isDirectory;

    private String path;
}
