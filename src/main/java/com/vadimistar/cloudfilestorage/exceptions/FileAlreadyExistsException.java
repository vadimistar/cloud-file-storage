package com.vadimistar.cloudfilestorage.exceptions;

public class FileAlreadyExistsException extends ResourceAlreadyExistsException {

    public FileAlreadyExistsException(String message) {
        super(message);
    }

    public FileAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
