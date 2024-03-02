package com.vadimistar.cloudfilestorage.exceptions;

public class FolderAlreadyExistsException extends ResourceAlreadyExistsException {

    public FolderAlreadyExistsException(String message) {
        super(message);
    }

    public FolderAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
