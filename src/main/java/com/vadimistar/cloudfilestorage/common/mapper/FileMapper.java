package com.vadimistar.cloudfilestorage.common.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.minio.util.MinioUtils;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileDto makeFileDto(ListObjectsResponseDto listObjectsResponseDto) {
        return FileDto.builder()
                .name(MinioUtils.getNormalFilename(listObjectsResponseDto.getName()))
                .isFolder(MinioUtils.isDirectory(listObjectsResponseDto))
                .path(MinioUtils.getNormalPath(listObjectsResponseDto.getName()))
                .build();
    }
}
