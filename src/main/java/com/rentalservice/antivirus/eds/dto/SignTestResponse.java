package com.rentalservice.antivirus.eds.dto;

public class SignTestResponse {

    private final String signature;

    public SignTestResponse(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
