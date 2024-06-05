package com.ashapiro.catanserver.exceptions.rest;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IncorrectTokenException extends RuntimeException{
    public IncorrectTokenException(String message) {
        super(message);
    }

    public IncorrectTokenException() {
    }
}
