package com.vadimistar.cloudfilestorage.folder.exception;

import lombok.Getter;

@Getter
public class UploadFolderException extends RuntimeException {

    private final String path;

    public UploadFolderException(String message, String path) {
        super(message);
        this.path = path;
    }
}
