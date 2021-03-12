package com.reznichenko.library.server.exception;

public class NoSuchBookException extends Exception {
    public NoSuchBookException(String message) {
        super(message);
    }

    public NoSuchBookException(String message, Throwable cause) {
        super(message, cause);
    }
}
