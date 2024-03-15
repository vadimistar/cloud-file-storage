package com.vadimistar.cloudfilestorage.file.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileViewRequestDto {

    @NotBlank(message = "Path cannot be blank")
    private String path;
}
