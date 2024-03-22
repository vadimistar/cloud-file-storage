package com.vadimistar.cloudfilestorage.security.exception;

public class UserNotLoggedInException extends RuntimeException {

    public UserNotLoggedInException(String message) {
        super(message);
    }
}
