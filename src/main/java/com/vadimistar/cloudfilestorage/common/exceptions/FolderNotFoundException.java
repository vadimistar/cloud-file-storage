package com.vadimistar.cloudfilestorage.common.exceptions;

public class FolderNotFoundException extends ResourceNotFoundException {

    public FolderNotFoundException(String message) {
        super(message);
    }

    public FolderNotFoundException() {
        this("Folder is not found");
    }
}
