package com.vadimistar.cloudfilestorage.folder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ViewFolderRequestDto {

    @NotBlank(message = "Path cannot be blank")
    private String path;
}
