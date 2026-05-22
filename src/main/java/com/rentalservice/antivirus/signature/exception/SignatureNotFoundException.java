package com.rentalservice.antivirus.signature.exception;

import java.util.UUID;

public class SignatureNotFoundException extends SignatureException {

    public SignatureNotFoundException(UUID id) {
        super("Malware signature not found: " + id);
    }
}
