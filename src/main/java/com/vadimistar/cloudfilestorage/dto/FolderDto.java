package com.vadimistar.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderDto {

    private String name;

    private String path;
}
