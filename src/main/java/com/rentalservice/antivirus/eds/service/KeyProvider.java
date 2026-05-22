package com.rentalservice.antivirus.eds.service;

import com.rentalservice.antivirus.eds.exception.EdsConfigurationException;
import com.rentalservice.antivirus.eds.properties.EdsProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class KeyProvider {

    private final EdsProperties edsProperties;
    private final ResourceLoader resourceLoader;

    private volatile KeyMaterial cachedKeyMaterial;

    public KeyProvider(EdsProperties edsProperties, ResourceLoader resourceLoader) {
        this.edsProperties = edsProperties;
        this.resourceLoader = resourceLoader;
    }

    public PrivateKey getPrivateKey() {
        return getKeyMaterial().privateKey();
    }

    public PublicKey getPublicKey() {
        return getCertificate().getPublicKey();
    }

    public X509Certificate getCertificate() {
        return getKeyMaterial().certificate();
    }

    public String getCertificatePem() {
        byte[] encodedCertificate;
        try {
            encodedCertificate = getCertificate().getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new EdsConfigurationException("Failed to encode EDS certificate", ex);
        }

        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encodedCertificate);
        return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----";
    }

    public String getAlias() {
        return edsProperties.getKeyAlias();
    }

    private KeyMaterial getKeyMaterial() {
        KeyMaterial material = cachedKeyMaterial;
        if (material != null) {
            return material;
        }

        synchronized (this) {
            if (cachedKeyMaterial == null) {
                cachedKeyMaterial = loadKeyMaterial();
            }
            return cachedKeyMaterial;
        }
    }

    private KeyMaterial loadKeyMaterial() {
        validateRequiredProperties();

        Resource resource = resourceLoader.getResource(edsProperties.getKeyStorePath());
        if (!resource.exists()) {
            throw new EdsConfigurationException("EDS keystore not found at path: " + edsProperties.getKeyStorePath());
        }

        try (InputStream inputStream = resource.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance(edsProperties.getKeyStoreType());
            keyStore.load(inputStream, edsProperties.getKeyStorePassword().toCharArray());

            String alias = edsProperties.getKeyAlias();
            if (!keyStore.containsAlias(alias)) {
                throw new EdsConfigurationException("EDS key alias not found in keystore: " + alias);
            }

            Key key = keyStore.getKey(alias, resolveKeyPassword());
            if (!(key instanceof PrivateKey privateKey)) {
                throw new EdsConfigurationException("Alias '" + alias + "' does not contain a private key");
            }

            Certificate certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate x509Certificate)) {
                throw new EdsConfigurationException("Alias '" + alias + "' does not contain an X.509 certificate");
            }

            return new KeyMaterial(privateKey, x509Certificate);
        } catch (IOException ex) {
            throw new EdsConfigurationException(
                    "Unable to load EDS keystore. Check key-store-path and key-store-password.",
                    ex
            );
        } catch (GeneralSecurityException ex) {
            throw new EdsConfigurationException(
                    "Unable to read EDS key material. Check key-store-type, key-alias, and key-password.",
                    ex
            );
        }
    }

    private void validateRequiredProperties() {
        if (!StringUtils.hasText(edsProperties.getKeyStorePath())) {
            throw new EdsConfigurationException("EDS key-store-path is not configured");
        }
        if (!StringUtils.hasText(edsProperties.getKeyStoreType())) {
            throw new EdsConfigurationException("EDS key-store-type is not configured");
        }
        if (!StringUtils.hasText(edsProperties.getKeyStorePassword())) {
            throw new EdsConfigurationException("EDS key-store-password is not configured");
        }
        if (!StringUtils.hasText(edsProperties.getKeyAlias())) {
            throw new EdsConfigurationException("EDS key-alias is not configured");
        }
    }

    private char[] resolveKeyPassword() {
        String keyPassword = StringUtils.hasText(edsProperties.getKeyPassword())
                ? edsProperties.getKeyPassword()
                : edsProperties.getKeyStorePassword();
        return keyPassword.toCharArray();
    }

    private record KeyMaterial(PrivateKey privateKey, X509Certificate certificate) {
    }
}
