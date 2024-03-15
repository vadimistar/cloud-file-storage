package com.vadimistar.cloudfilestorage.folder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFolderRequestDto {

    @NotNull(message = "No files provided")
    private final MultipartFile[] files;

    @NotNull(message = "Path is not provided")
    private final String path;
}
