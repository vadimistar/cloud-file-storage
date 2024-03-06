package com.vadimistar.cloudfilestorage.minio.mapper;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ListObjectsResponseDtoMapper {

    public static ListObjectsResponseDto makeListObjectsResponseDto(Item item) {
        return ListObjectsResponseDto.builder()
                .name(item.objectName())
                .size(item.size())
                .isDirectory(item.isDir())
                .build();
    }
}
