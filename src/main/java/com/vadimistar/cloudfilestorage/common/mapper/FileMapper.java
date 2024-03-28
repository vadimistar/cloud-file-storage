package com.vadimistar.cloudfilestorage.common.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.minio.dto.MinioObjectDto;
import com.vadimistar.cloudfilestorage.common.utils.MinioUtils;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileDto makeFileDto(MinioObjectDto minioObjectDto) {
        return FileDto.builder()
                .name(MinioUtils.getNormalFilename(minioObjectDto.getName()))
                .isFolder(MinioUtils.isDirectory(minioObjectDto))
                .path(MinioUtils.getNormalPath(minioObjectDto.getName()))
                .build();
    }
}
