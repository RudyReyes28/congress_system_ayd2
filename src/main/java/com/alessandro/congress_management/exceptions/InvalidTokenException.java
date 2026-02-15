package com.alessandro.congress_management.exceptions;

public class InvalidTokenException extends ServiceException {
    public InvalidTokenException() {
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
