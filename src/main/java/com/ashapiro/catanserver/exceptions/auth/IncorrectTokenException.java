package com.ashapiro.catanserver.exceptions.auth;

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
