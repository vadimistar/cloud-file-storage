package com.vadimistar.cloudfilestorage.common.exceptions;

public class FolderAlreadyExistsException extends ResourceAlreadyExistsException {

    public FolderAlreadyExistsException(String message, String path) {
        super(message, path);
    }
}
