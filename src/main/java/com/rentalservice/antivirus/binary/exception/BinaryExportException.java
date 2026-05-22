package com.rentalservice.antivirus.binary.exception;

public class BinaryExportException extends RuntimeException {

    public BinaryExportException(String message) {
        super(message);
    }

    public BinaryExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
