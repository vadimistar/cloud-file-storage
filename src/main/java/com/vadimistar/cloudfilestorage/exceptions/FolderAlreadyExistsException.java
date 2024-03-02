package com.vadimistar.cloudfilestorage.exceptions;

public class FolderAlreadyExistsException extends ResourceAlreadyExistsException {

    public FolderAlreadyExistsException(String message, String path) {
        super(message, path);
    }
}
