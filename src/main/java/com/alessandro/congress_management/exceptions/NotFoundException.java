package com.alessandro.congress_management.exceptions;

public class NotFoundException extends ServiceException {

    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

}