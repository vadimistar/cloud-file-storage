package com.vadimistar.cloudfilestorage.folder.exception;

import com.vadimistar.cloudfilestorage.common.exception.ResourceNotFoundException;

public class FolderNotFoundException extends ResourceNotFoundException {

    public FolderNotFoundException(String message) {
        super(message);
    }
}
