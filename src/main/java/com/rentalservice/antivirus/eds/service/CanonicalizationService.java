package com.rentalservice.antivirus.eds.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalservice.antivirus.eds.exception.EdsOperationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class CanonicalizationService {

    private final ObjectMapper objectMapper;

    public CanonicalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] canonicalizeToBytes(Object payload) {
        return canonicalizeToJson(payload).getBytes(StandardCharsets.UTF_8);
    }

    public String canonicalizeToJson(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            return new org.erdtman.jcs.JsonCanonicalizer(json).getEncodedString();
        } catch (Exception ex) {
            throw new EdsOperationException(
                    "Failed to canonicalize payload for EDS signing",
                    ex
            );
        }
    }
}