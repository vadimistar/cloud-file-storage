package com.vadimistar.cloudfilestorage.common.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.common.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.common.util.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileDtoMapper {

    public static FileDto makeFileDto(ListObjectsResponseDto listObjectsResponseDto) {
        return FileDto.builder()
                .name(PathUtils.getFilename(listObjectsResponseDto.getName()))
                .isFolder(MinioUtils.isDirectory(listObjectsResponseDto))
                .path(MinioUtils.getNormalPath(listObjectsResponseDto.getName()))
                .build();
    }
}
