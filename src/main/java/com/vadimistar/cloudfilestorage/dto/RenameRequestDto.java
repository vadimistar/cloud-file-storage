package com.vadimistar.cloudfilestorage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameRequestDto {

    @NotBlank(message = "Path cannot be blank")
    private String path;

    @NotBlank(message = "Name cannot be blank")
    private String name;
}
