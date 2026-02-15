package com.alessandro.congress_management.exceptions;

public class DuplicatedEntityException extends ServiceException {
    public DuplicatedEntityException() {
    }

    public DuplicatedEntityException(String message) {
        super(message);
    }
}
