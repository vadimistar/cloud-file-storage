package com.vadimistar.cloudfilestorage.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RenameFileRequestDto {

    @NotBlank(message = "Path cannot be blank")
    private String path;

    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^([0-9]|[A-Z]|[a-z]|[!\\-_ .*'\\(\\)])+$", message = "Name contains invalid characters")
    private String name;
}
