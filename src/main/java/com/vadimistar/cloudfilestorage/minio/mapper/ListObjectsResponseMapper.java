package com.vadimistar.cloudfilestorage.minio.mapper;

import com.vadimistar.cloudfilestorage.minio.dto.MinioObjectDto;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ListObjectsResponseMapper {

    public MinioObjectDto makeMinioObjectDto(Item item) {
        return MinioObjectDto.builder()
                .name(item.objectName())
                .size(item.size())
                .isDirectory(item.isDir())
                .build();
    }
}
