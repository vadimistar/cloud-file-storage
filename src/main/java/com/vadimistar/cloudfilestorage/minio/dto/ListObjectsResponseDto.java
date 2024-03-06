package com.vadimistar.cloudfilestorage.minio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListObjectsResponseDto {

    private final String name;

    private final long size;

    private final boolean isDirectory;
}
