package com.rentalservice.antivirus.eds.controller;

import com.rentalservice.antivirus.eds.dto.CertificateResponse;
import com.rentalservice.antivirus.eds.dto.SignTestRequest;
import com.rentalservice.antivirus.eds.dto.SignTestResponse;
import com.rentalservice.antivirus.eds.dto.VerifyTestRequest;
import com.rentalservice.antivirus.eds.dto.VerifyTestResponse;
import com.rentalservice.antivirus.eds.service.KeyProvider;
import com.rentalservice.antivirus.eds.service.SigningService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/api/eds")
@PreAuthorize("hasRole('ADMIN')")
public class EdsController {

    private final SigningService signingService;
    private final KeyProvider keyProvider;

    public EdsController(SigningService signingService, KeyProvider keyProvider) {
        this.signingService = signingService;
        this.keyProvider = keyProvider;
    }

    @PostMapping("/sign-test")
    public SignTestResponse signTest(@Valid @RequestBody SignTestRequest request) {
        return new SignTestResponse(signingService.signObject(request.getPayload()));
    }

    @PostMapping("/verify-test")
    public VerifyTestResponse verifyTest(@Valid @RequestBody VerifyTestRequest request) {
        boolean valid = signingService.verifyObject(request.getPayload(), request.getSignature());
        return new VerifyTestResponse(valid);
    }

    @GetMapping("/certificate")
    public CertificateResponse getCertificate() {
        X509Certificate certificate = keyProvider.getCertificate();
        return new CertificateResponse(
                keyProvider.getAlias(),
                certificate.getSigAlgName(),
                certificate.getType(),
                certificate.getSubjectX500Principal().getName(),
                certificate.getIssuerX500Principal().getName(),
                certificate.getSerialNumber().toString(16),
                certificate.getNotBefore().toInstant(),
                certificate.getNotAfter().toInstant(),
                keyProvider.getCertificatePem()
        );
    }
}
