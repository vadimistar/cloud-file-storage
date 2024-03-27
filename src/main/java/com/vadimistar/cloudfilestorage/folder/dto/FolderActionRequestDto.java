package com.vadimistar.cloudfilestorage.folder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FolderActionRequestDto {

    @NotBlank(message = "Path cannot be blank")
    private String path;
}
