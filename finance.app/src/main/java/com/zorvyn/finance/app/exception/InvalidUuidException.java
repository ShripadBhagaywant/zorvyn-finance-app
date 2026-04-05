package com.zorvyn.finance.app.exception;

public class InvalidUuidException extends RuntimeException{

    public InvalidUuidException(String msg)
    {
        super(msg);
    }
}
