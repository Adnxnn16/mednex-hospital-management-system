package com.mednex.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMedicalHistoryException extends RuntimeException {
    public InvalidMedicalHistoryException(String message) {
        super(message);
    }
}
