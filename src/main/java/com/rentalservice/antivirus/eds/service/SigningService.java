package com.rentalservice.antivirus.eds.service;

import com.rentalservice.antivirus.eds.exception.EdsConfigurationException;
import com.rentalservice.antivirus.eds.exception.EdsOperationException;
import com.rentalservice.antivirus.eds.properties.EdsProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

@Service
public class SigningService {

    private final KeyProvider keyProvider;
    private final CanonicalizationService canonicalizationService;
    private final EdsProperties edsProperties;

    public SigningService(KeyProvider keyProvider,
                          CanonicalizationService canonicalizationService,
                          EdsProperties edsProperties) {
        this.keyProvider = keyProvider;
        this.canonicalizationService = canonicalizationService;
        this.edsProperties = edsProperties;
    }

    public String signObject(Object payload) {
        byte[] signature = signBytes(canonicalizationService.canonicalizeToBytes(payload));
        return Base64.getEncoder().encodeToString(signature);
    }

    public byte[] signBytes(byte[] payload) {
        try {
            Signature signature = Signature.getInstance(resolveAlgorithm());
            signature.initSign(keyProvider.getPrivateKey());
            signature.update(payload);
            return signature.sign();
        } catch (GeneralSecurityException ex) {
            throw new EdsOperationException("Failed to sign payload using configured EDS algorithm", ex);
        }
    }

    public boolean verifyObject(Object payload, String base64Signature) {
        if (!StringUtils.hasText(base64Signature)) {
            return false;
        }

        try {
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
            Signature signature = Signature.getInstance(resolveAlgorithm());
            signature.initVerify(keyProvider.getPublicKey());
            signature.update(canonicalizationService.canonicalizeToBytes(payload));
            return signature.verify(signatureBytes);
        } catch (IllegalArgumentException ex) {
            return false;
        } catch (GeneralSecurityException ex) {
            throw new EdsOperationException("Failed to verify payload using configured EDS algorithm", ex);
        }
    }

    private String resolveAlgorithm() {
        if (!StringUtils.hasText(edsProperties.getAlgorithm())) {
            throw new EdsConfigurationException("EDS algorithm is not configured");
        }
        return edsProperties.getAlgorithm();
    }
}
