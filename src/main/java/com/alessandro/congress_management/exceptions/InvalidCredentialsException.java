package com.alessandro.congress_management.exceptions;

public class InvalidCredentialsException extends ServiceException {
    public InvalidCredentialsException() {
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
