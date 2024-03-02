package com.vadimistar.cloudfilestorage.exceptions;

public class FolderNotFoundException extends ResourceNotFoundException {

    public FolderNotFoundException(String message) {
        super(message);
    }

    public FolderNotFoundException() {
        super("Folder is not found");
    }
}
