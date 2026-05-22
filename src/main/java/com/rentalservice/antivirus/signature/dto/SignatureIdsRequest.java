package com.rentalservice.antivirus.signature.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class SignatureIdsRequest {

    @NotEmpty(message = "ids must not be empty")
    private List<@NotNull(message = "signature id must not be null") UUID> ids;

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }
}
