package com.rentalservice.antivirus.eds.exception;

public class EdsOperationException extends EdsException {

    public EdsOperationException(String message) {
        super(message);
    }

    public EdsOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
