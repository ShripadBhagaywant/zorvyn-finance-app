package com.zorvyn.finance.app.exception;

public class TokenBlackListedException extends RuntimeException {

    public TokenBlackListedException(String msg)
    {
        super(msg);
    }
}
