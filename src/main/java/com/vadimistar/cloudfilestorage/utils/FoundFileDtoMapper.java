package com.vadimistar.cloudfilestorage.utils;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.FoundFileDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FoundFileDtoMapper {

    public static FoundFileDto makeFoundFileDto(FileDto fileDto) {
        return FoundFileDto.builder()
                .name(fileDto.getName())
                .isFolder(fileDto.isFolder())
                .parentPath(PathUtils.getParentDirectory(fileDto.getPath()))
                .build();
    }
}
