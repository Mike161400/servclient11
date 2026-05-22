package com.rentalservice.antivirus.eds.service;

import com.rentalservice.antivirus.eds.properties.EdsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.security.Signature;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SigningServiceTest {

    private SigningService signingService;
    private CanonicalizationService canonicalizationService;
    private KeyProvider keyProvider;

    @BeforeEach
    void setUp() {
        EdsProperties properties = new EdsProperties();
        properties.setKeyStorePath("classpath:eds/test-keystore.p12");
        properties.setKeyStoreType("PKCS12");
        properties.setKeyStorePassword("changeit");
        properties.setKeyAlias("antivirus-test");
        properties.setKeyPassword("changeit");
        properties.setAlgorithm("SHA256withRSA");

        canonicalizationService = new CanonicalizationService();
        keyProvider = new KeyProvider(properties, new DefaultResourceLoader());
        signingService = new SigningService(keyProvider, canonicalizationService, properties);
    }

    @Test
    void sameLogicalObjectShouldProduceSameSignature() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("b", 2);
        first.put("a", Map.of("z", true, "y", "value"));

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("a", Map.of("y", "value", "z", true));
        second.put("b", 2);

        String firstSignature = signingService.signObject(first);
        String secondSignature = signingService.signObject(second);

        assertEqualsCanonicalBytes(first, second);
        assertArrayEquals(firstSignature.getBytes(), secondSignature.getBytes());
    }

    @Test
    void differentDataShouldProduceDifferentSignature() {
        Map<String, Object> first = Map.of("threatName", "Trojan.A", "offsetStart", 1);
        Map<String, Object> second = Map.of("threatName", "Trojan.B", "offsetStart", 1);

        String firstSignature = signingService.signObject(first);
        String secondSignature = signingService.signObject(second);

        assertNotEquals(firstSignature, secondSignature);
    }

    @Test
    void verifyObjectShouldReturnTrueForCorrectSignature() {
        Map<String, Object> payload = Map.of(
                "threatName", "Eicar.Test.File",
                "fileType", "exe",
                "offsetStart", 0,
                "offsetEnd", 68
        );

        String signature = signingService.signObject(payload);

        assertTrue(signingService.verifyObject(payload, signature));
        assertFalse(signingService.verifyObject(Map.of("threatName", "Modified"), signature));
    }

    @Test
    void signBytesShouldWorkForManifestBytes() throws Exception {
        byte[] manifestBytes = canonicalizationService.canonicalizeToBytes(Map.of(
                "magic", "AVSIG",
                "version", 1,
                "recordCount", 2
        ));

        byte[] signatureBytes = signingService.signBytes(manifestBytes);

        assertTrue(signatureBytes.length > 0);

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(keyProvider.getPublicKey());
        verifier.update(manifestBytes);
        assertTrue(verifier.verify(signatureBytes));
    }

    private void assertEqualsCanonicalBytes(Object first, Object second) {
        assertArrayEquals(
                canonicalizationService.canonicalizeToBytes(first),
                canonicalizationService.canonicalizeToBytes(second)
        );
    }
}
