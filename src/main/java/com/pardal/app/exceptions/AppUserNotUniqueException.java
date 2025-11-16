package com.pardal.app.exceptions;

public class AppUserNotUniqueException extends RuntimeException {
    public AppUserNotUniqueException(String message) {
        super(message);
    }
}
