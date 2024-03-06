package com.vadimistar.cloudfilestorage.common.mapper;

import com.vadimistar.cloudfilestorage.common.dto.ListObjectsResponseDto;
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
