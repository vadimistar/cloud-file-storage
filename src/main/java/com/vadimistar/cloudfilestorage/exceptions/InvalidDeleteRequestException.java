package com.vadimistar.cloudfilestorage.exceptions;

public class InvalidDeleteRequestException extends FileActionException {

    public InvalidDeleteRequestException(String message, String path) {
        super(message, path);
    }
}
