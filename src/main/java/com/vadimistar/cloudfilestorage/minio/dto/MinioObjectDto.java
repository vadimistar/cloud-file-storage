package com.vadimistar.cloudfilestorage.minio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinioObjectDto {

    private String name;
    private long size;
    private boolean isDirectory;
}
