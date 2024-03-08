package com.vadimistar.cloudfilestorage.common.exception;

import lombok.Getter;

@Getter
public class FileActionException extends RuntimeException {

    private final String path;

    public FileActionException(String message, String path) {
        super(message);
        this.path = path;
    }
}
