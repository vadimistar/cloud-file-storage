package com.vadimistar.cloudfilestorage.exceptions;

import lombok.Getter;

@Getter
public class UploadFileException extends RuntimeException {

    private final String path;

    public UploadFileException(String message, String path) {
        super(message);
        this.path = path;
    }
}
