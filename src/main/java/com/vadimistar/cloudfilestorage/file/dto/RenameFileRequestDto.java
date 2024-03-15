package com.vadimistar.cloudfilestorage.file.dto;

import com.vadimistar.cloudfilestorage.validation.NotContainsSlash;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameFileRequestDto {

    @NotBlank(message = "Path cannot be blank")
    private String path;

    @NotBlank(message = "Name cannot be blank")
    @NotContainsSlash(message = "Name cannot contain slash")
    private String name;
}
