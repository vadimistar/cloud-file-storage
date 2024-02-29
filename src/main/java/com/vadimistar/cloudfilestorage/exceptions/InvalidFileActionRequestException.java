package com.vadimistar.cloudfilestorage.exceptions;

public class InvalidFileActionRequestException extends FileActionException {

    public InvalidFileActionRequestException(String message, String path) {
        super(message, path);
    }
}
