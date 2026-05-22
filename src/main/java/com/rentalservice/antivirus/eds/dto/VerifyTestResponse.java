package com.rentalservice.antivirus.eds.dto;

public class VerifyTestResponse {

    private final boolean valid;

    public VerifyTestResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}
