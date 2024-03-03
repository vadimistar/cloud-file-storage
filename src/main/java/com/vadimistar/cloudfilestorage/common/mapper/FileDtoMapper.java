package com.vadimistar.cloudfilestorage.common.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.common.util.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileDtoMapper {

    public static FileDto makeFileDto(Item item) {
        return FileDto.builder()
                .name(PathUtils.getFilename(item.objectName()))
                .isFolder(MinioUtils.isDirectory(item))
                .path(MinioUtils.getNormalPath(item.objectName()))
                .build();
    }
}
