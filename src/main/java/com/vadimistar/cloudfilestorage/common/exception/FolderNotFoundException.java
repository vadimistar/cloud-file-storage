package com.vadimistar.cloudfilestorage.common.exception;

public class FolderNotFoundException extends ResourceNotFoundException {

    public FolderNotFoundException(String message) {
        super(message);
    }

    public FolderNotFoundException() {
        this("Folder is not found");
    }
}
