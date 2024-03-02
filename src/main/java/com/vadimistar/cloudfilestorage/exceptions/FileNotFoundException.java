package com.vadimistar.cloudfilestorage.exceptions;

public class FileNotFoundException extends ResourceNotFoundException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException() {
        super("File is not found");
    }
}
