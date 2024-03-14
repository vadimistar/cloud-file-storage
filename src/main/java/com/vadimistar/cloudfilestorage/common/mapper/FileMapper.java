package com.vadimistar.cloudfilestorage.common.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.minio.utils.MinioUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileMapper {

    public static FileDto makeFileDto(ListObjectsResponseDto listObjectsResponseDto) {
        return FileDto.builder()
                .name(MinioUtils.getNormalFilename(listObjectsResponseDto.getName()))
                .isFolder(MinioUtils.isDirectory(listObjectsResponseDto))
                .path(MinioUtils.getNormalPath(listObjectsResponseDto.getName()))
                .build();
    }
}
