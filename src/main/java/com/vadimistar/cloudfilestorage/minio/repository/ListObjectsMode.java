package com.vadimistar.cloudfilestorage.minio.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ListObjectsMode {
    NON_RECURSIVE(false),
    RECURSIVE(true);

    private final boolean recursive;
}
