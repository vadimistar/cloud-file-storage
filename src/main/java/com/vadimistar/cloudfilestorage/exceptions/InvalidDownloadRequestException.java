package com.vadimistar.cloudfilestorage.exceptions;

public class InvalidDownloadRequestException extends FileActionException {

    public InvalidDownloadRequestException(String message, String path) {
        super(message, path);
    }
}
