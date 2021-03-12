package com.reznichenko.library.server.exception;

public class BookAlreadyBorrowedException extends Exception {
    public BookAlreadyBorrowedException(String message) {
        super(message);
    }

    public BookAlreadyBorrowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
