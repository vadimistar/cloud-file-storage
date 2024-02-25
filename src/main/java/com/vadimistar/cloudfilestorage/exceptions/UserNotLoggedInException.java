package com.vadimistar.cloudfilestorage.exceptions;

public class UserNotLoggedInException extends RuntimeException {

    public UserNotLoggedInException(String message) {
        super(message);
    }

    public UserNotLoggedInException() {
        super("You are not logged in, please login again");
    }
}
