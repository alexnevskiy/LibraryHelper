package com.poly.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedArgumentException extends RuntimeException {

    public UnsupportedArgumentException(String message) {
        super(message);
    }
}
