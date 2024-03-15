package com.vadimistar.cloudfilestorage.security.exception;

public class UserNotLoggedInException extends RuntimeException {

    public UserNotLoggedInException(String message) {
        super(message);
    }

    public UserNotLoggedInException() {
        this("You are not logged in, please login again");
    }
}
