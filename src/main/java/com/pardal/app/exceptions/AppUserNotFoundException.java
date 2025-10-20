package com.pardal.app.exceptions;

public class AppUserNotFoundException extends RuntimeException {
    public AppUserNotFoundException(String message) {
        super(message);
    }
}
