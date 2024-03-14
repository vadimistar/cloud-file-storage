package com.vadimistar.cloudfilestorage.search.mapper;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FoundFileMapper {

    public static FoundFileDto makeFoundFileDto(FileDto fileDto) {
        return FoundFileDto.builder()
                .name(fileDto.getName())
                .isFolder(fileDto.isFolder())
                .parentPath(PathUtils.getParentDirectory(fileDto.getPath()))
                .build();
    }
}
