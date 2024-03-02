package com.vadimistar.cloudfilestorage.exceptions;

public class FileAlreadyExistsException extends ResourceAlreadyExistsException {

    public FileAlreadyExistsException(String message, String path) {
        super(message, path);
    }
}

