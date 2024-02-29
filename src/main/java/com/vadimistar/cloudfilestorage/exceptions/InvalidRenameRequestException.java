package com.vadimistar.cloudfilestorage.exceptions;

public class InvalidRenameRequestException extends FileActionException {

    public InvalidRenameRequestException(String message, String path) {
        super(message, path);
    }
}
