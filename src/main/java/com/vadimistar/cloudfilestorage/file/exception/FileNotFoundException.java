package com.vadimistar.cloudfilestorage.file.exception;

import com.vadimistar.cloudfilestorage.common.exception.ResourceNotFoundException;

public class FileNotFoundException extends ResourceNotFoundException {

    public FileNotFoundException(String message) {
        super(message);
    }
}
