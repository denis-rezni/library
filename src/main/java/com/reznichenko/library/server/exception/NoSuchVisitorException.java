package com.reznichenko.library.server.exception;

public class NoSuchVisitorException extends Exception {
    public NoSuchVisitorException(String message) {
        super(message);
    }

    public NoSuchVisitorException(String message, Throwable cause) {
        super(message, cause);
    }
}
