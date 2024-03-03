package com.vadimistar.cloudfilestorage.common.exceptions;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {

    private final String path;

    public ResourceAlreadyExistsException(String message, String path) {
        super(message);
        this.path = path;
    }
}
