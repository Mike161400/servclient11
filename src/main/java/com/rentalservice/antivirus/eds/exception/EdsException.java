package com.rentalservice.antivirus.eds.exception;

public class EdsException extends RuntimeException {

    public EdsException(String message) {
        super(message);
    }

    public EdsException(String message, Throwable cause) {
        super(message, cause);
    }
}
