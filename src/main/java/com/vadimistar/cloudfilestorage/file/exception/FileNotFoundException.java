package com.vadimistar.cloudfilestorage.file.exception;

import com.vadimistar.cloudfilestorage.common.exceptions.ResourceNotFoundException;

public class FileNotFoundException extends ResourceNotFoundException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException() {
        this("File is not found");
    }
}
