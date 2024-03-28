package com.vadimistar.cloudfilestorage.search.mapper;

import com.vadimistar.cloudfilestorage.common.utils.MinioUtils;
import com.vadimistar.cloudfilestorage.minio.dto.MinioObjectDto;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.common.utils.path.PathUtils;
import org.springframework.stereotype.Component;

@Component
public class FoundFileMapper {

    public FoundFileDto makeFoundFileDto(MinioObjectDto minioObjectDto) {
        String normalPath = MinioUtils.getNormalPath(minioObjectDto.getName());
        return FoundFileDto.builder()
                .name(PathUtils.getFilename(normalPath))
                .isFolder(MinioUtils.isDirectory(minioObjectDto))
                .parentPath(PathUtils.getParentDirectory(normalPath))
                .build();
    }
}
