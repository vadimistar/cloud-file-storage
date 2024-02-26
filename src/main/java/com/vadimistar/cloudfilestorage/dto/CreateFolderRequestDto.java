package com.vadimistar.cloudfilestorage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFolderRequestDto {

    @NotBlank
    String path;
}
