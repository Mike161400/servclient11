package com.rentalservice.antivirus.eds.dto;

import java.time.Instant;

public class CertificateResponse {

    private final String alias;
    private final String algorithm;
    private final String type;
    private final String subjectDn;
    private final String issuerDn;
    private final String serialNumberHex;
    private final Instant notBefore;
    private final Instant notAfter;
    private final String pem;

    public CertificateResponse(String alias,
                               String algorithm,
                               String type,
                               String subjectDn,
                               String issuerDn,
                               String serialNumberHex,
                               Instant notBefore,
                               Instant notAfter,
                               String pem) {
        this.alias = alias;
        this.algorithm = algorithm;
        this.type = type;
        this.subjectDn = subjectDn;
        this.issuerDn = issuerDn;
        this.serialNumberHex = serialNumberHex;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.pem = pem;
    }

    public String getAlias() {
        return alias;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getType() {
        return type;
    }

    public String getSubjectDn() {
        return subjectDn;
    }

    public String getIssuerDn() {
        return issuerDn;
    }

    public String getSerialNumberHex() {
        return serialNumberHex;
    }

    public Instant getNotBefore() {
        return notBefore;
    }

    public Instant getNotAfter() {
        return notAfter;
    }

    public String getPem() {
        return pem;
    }
}
