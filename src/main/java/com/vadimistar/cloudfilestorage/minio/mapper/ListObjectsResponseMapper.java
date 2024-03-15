package com.vadimistar.cloudfilestorage.minio.mapper;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ListObjectsResponseMapper {

    public ListObjectsResponseDto makeListObjectsResponseDto(Item item) {
        return ListObjectsResponseDto.builder()
                .name(item.objectName())
                .size(item.size())
                .isDirectory(item.isDir() || item.size() == 0)
                .build();
    }
}
