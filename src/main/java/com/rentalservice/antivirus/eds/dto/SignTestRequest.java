package com.rentalservice.antivirus.eds.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public class SignTestRequest {

    @NotNull(message = "Payload is required")
    private JsonNode payload;

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
