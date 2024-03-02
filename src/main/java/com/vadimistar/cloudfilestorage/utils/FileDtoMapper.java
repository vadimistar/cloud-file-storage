package com.vadimistar.cloudfilestorage.utils;

import com.vadimistar.cloudfilestorage.dto.FileDto;
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
