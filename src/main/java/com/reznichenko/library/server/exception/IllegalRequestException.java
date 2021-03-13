package com.reznichenko.library.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalRequestException extends RuntimeException {
    public IllegalRequestException(Throwable cause) {
        super("bad request: " + cause.getMessage(), cause);
    }
}
