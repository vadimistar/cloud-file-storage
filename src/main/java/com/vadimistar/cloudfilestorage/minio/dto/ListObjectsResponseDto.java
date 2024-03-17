package com.vadimistar.cloudfilestorage.minio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListObjectsResponseDto {

    private String name;
    private long size;
    private boolean isDirectory;
}
