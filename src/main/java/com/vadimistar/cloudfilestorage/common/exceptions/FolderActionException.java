package com.vadimistar.cloudfilestorage.common.exceptions;

import lombok.Getter;

@Getter
public class FolderActionException extends RuntimeException {

    private final String path;

    public FolderActionException(String message, String path) {
        super(message);
        this.path = path;
    }
}
