package com.vadimistar.cloudfilestorage.search.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import org.springframework.stereotype.Component;

@Component
public class FoundFileMapper {

    public FoundFileDto makeFoundFileDto(FileDto fileDto) {
        return FoundFileDto.builder()
                .name(fileDto.getName())
                .isFolder(fileDto.isFolder())
                .parentPath(PathUtils.getParentDirectory(fileDto.getPath()))
                .build();
    }
}
