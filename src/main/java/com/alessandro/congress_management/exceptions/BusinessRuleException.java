package com.alessandro.congress_management.exceptions;

public class BusinessRuleException extends ServiceException{

    public BusinessRuleException() {
    }

    public BusinessRuleException(String message) {
        super(message);
    }
}
