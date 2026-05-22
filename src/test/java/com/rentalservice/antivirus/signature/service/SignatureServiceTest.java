package com.rentalservice.antivirus.signature.service;

import com.rentalservice.antivirus.eds.properties.EdsProperties;
import com.rentalservice.antivirus.eds.service.CanonicalizationService;
import com.rentalservice.antivirus.eds.service.KeyProvider;
import com.rentalservice.antivirus.eds.service.SigningService;
import com.rentalservice.antivirus.signature.dto.CreateSignatureRequest;
import com.rentalservice.antivirus.signature.dto.SignatureIdsRequest;
import com.rentalservice.antivirus.signature.dto.UpdateSignatureRequest;
import com.rentalservice.antivirus.signature.entity.MalwareSignature;
import com.rentalservice.antivirus.signature.entity.MalwareSignatureAudit;
import com.rentalservice.antivirus.signature.entity.MalwareSignatureHistory;
import com.rentalservice.antivirus.signature.entity.SignatureStatus;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureAuditRepository;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureHistoryRepository;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SignatureServiceTest {

    private MalwareSignatureRepository signatureRepository;
    private MalwareSignatureHistoryRepository historyRepository;
    private MalwareSignatureAuditRepository auditRepository;
    private SigningService signingService;
    private SignatureService signatureService;

    @BeforeEach
    void setUp() {
        signatureRepository = mock(MalwareSignatureRepository.class);
        historyRepository = mock(MalwareSignatureHistoryRepository.class);
        auditRepository = mock(MalwareSignatureAuditRepository.class);

        EdsProperties properties = new EdsProperties();
        properties.setKeyStorePath("classpath:eds/test-keystore.p12");
        properties.setKeyStoreType("PKCS12");
        properties.setKeyStorePassword("changeit");
        properties.setKeyAlias("antivirus-test");
        properties.setKeyPassword("changeit");
        properties.setAlgorithm("SHA256withRSA");

        signingService = new SigningService(
                new KeyProvider(properties, new DefaultResourceLoader()),
                new CanonicalizationService(),
                properties
        );
        signatureService = new SignatureService(signatureRepository, historyRepository, auditRepository, signingService);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "n/a", List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldCreateAuditAndSignature() {
        CreateSignatureRequest request = createRequest();
        when(signatureRepository.save(any(MalwareSignature.class))).thenAnswer(invocation -> {
            MalwareSignature signature = invocation.getArgument(0);
            signature.setId(UUID.randomUUID());
            signature.setUpdatedAt(LocalDateTime.now());
            return signature;
        });

        var response = signatureService.create(request);

        ArgumentCaptor<MalwareSignature> signatureCaptor = ArgumentCaptor.forClass(MalwareSignature.class);
        verify(signatureRepository).save(signatureCaptor.capture());
        MalwareSignature saved = signatureCaptor.getValue();

        assertEquals(SignatureStatus.ACTUAL, saved.getStatus());
        assertNotNull(saved.getDigitalSignatureBase64());
        assertTrue(signingService.verifyObject(signingPayload(saved), saved.getDigitalSignatureBase64()));
        assertEquals("AA11", saved.getFirstBytesHex());
        assertNotNull(response.getId());

        ArgumentCaptor<MalwareSignatureAudit> auditCaptor = ArgumentCaptor.forClass(MalwareSignatureAudit.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertEquals("CREATE", auditCaptor.getValue().getAction());
        assertTrue(auditCaptor.getValue().getFieldsChanged().contains("digitalSignatureBase64"));
        verify(historyRepository, never()).save(any(MalwareSignatureHistory.class));
    }

    @Test
    void updateShouldCreateHistoryAuditAndNewSignature() {
        UUID id = UUID.randomUUID();
        MalwareSignature existing = existingSignature(id, SignatureStatus.ACTUAL);
        String oldSignature = existing.getDigitalSignatureBase64();
        UpdateSignatureRequest request = updateRequest();

        when(signatureRepository.findById(id)).thenReturn(Optional.of(existing));
        when(signatureRepository.save(any(MalwareSignature.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = signatureService.update(id, request);

        verify(historyRepository).save(any(MalwareSignatureHistory.class));
        ArgumentCaptor<MalwareSignatureAudit> auditCaptor = ArgumentCaptor.forClass(MalwareSignatureAudit.class);
        verify(auditRepository).save(auditCaptor.capture());

        assertNotNull(response.getDigitalSignatureBase64());
        assertTrue(!oldSignature.equals(response.getDigitalSignatureBase64()));
        assertEquals("Trojan.B", response.getThreatName());
        assertTrue(signingService.verifyObject(signingPayloadFromResponse(response), response.getDigitalSignatureBase64()));
        assertTrue(auditCaptor.getValue().getFieldsChanged().contains("threatName"));
        assertTrue(auditCaptor.getValue().getFieldsChanged().contains("digitalSignatureBase64"));
    }

    @Test
    void deleteShouldBeLogicalAndCreateHistoryAudit() {
        UUID id = UUID.randomUUID();
        MalwareSignature existing = existingSignature(id, SignatureStatus.ACTUAL);
        String oldSignature = existing.getDigitalSignatureBase64();

        when(signatureRepository.findById(id)).thenReturn(Optional.of(existing));
        when(signatureRepository.save(any(MalwareSignature.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = signatureService.logicalDelete(id);

        assertEquals(SignatureStatus.DELETED, response.getStatus());
        assertTrue(!oldSignature.equals(response.getDigitalSignatureBase64()));
        assertTrue(signingService.verifyObject(signingPayloadFromResponse(response), response.getDigitalSignatureBase64()));
        verify(historyRepository).save(any(MalwareSignatureHistory.class));
        verify(auditRepository).save(any(MalwareSignatureAudit.class));
    }

    @Test
    void fullShouldNotReturnDeleted() {
        MalwareSignature actual = existingSignature(UUID.randomUUID(), SignatureStatus.ACTUAL);
        when(signatureRepository.findAllByStatusOrderByUpdatedAtAsc(SignatureStatus.ACTUAL)).thenReturn(List.of(actual));

        var result = signatureService.getFullDatabase();

        assertEquals(1, result.size());
        assertEquals(SignatureStatus.ACTUAL, result.get(0).getStatus());
        verify(signatureRepository).findAllByStatusOrderByUpdatedAtAsc(SignatureStatus.ACTUAL);
    }

    @Test
    void incrementShouldReturnDeletedWhenUpdatedAfterSince() {
        Instant since = Instant.now().minusSeconds(3600);
        MalwareSignature actual = existingSignature(UUID.randomUUID(), SignatureStatus.ACTUAL);
        MalwareSignature deleted = existingSignature(UUID.randomUUID(), SignatureStatus.DELETED);
        when(signatureRepository.findAllByUpdatedAtAfterOrderByUpdatedAtAsc(any(LocalDateTime.class)))
                .thenReturn(List.of(actual, deleted));

        var result = signatureService.getIncrementSince(since);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(signature -> signature.getStatus() == SignatureStatus.DELETED));
    }

    @Test
    void getByIdsShouldPreserveRequestedOrderForFoundIds() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        MalwareSignature first = existingSignature(firstId, SignatureStatus.ACTUAL);
        MalwareSignature second = existingSignature(secondId, SignatureStatus.ACTUAL);

        when(signatureRepository.findAllById(List.of(secondId, firstId))).thenReturn(List.of(first, second));

        SignatureIdsRequest request = new SignatureIdsRequest();
        request.setIds(List.of(secondId, firstId));

        var result = signatureService.getByIds(request);

        assertEquals(List.of(secondId, firstId),
                result.stream().map(signature -> signature.getId()).toList());
    }

    private CreateSignatureRequest createRequest() {
        CreateSignatureRequest request = new CreateSignatureRequest();
        request.setThreatName("Trojan.A");
        request.setFirstBytesHex("aa11");
        request.setRemainderHashHex("bb22");
        request.setRemainderLength(128);
        request.setFileType("exe");
        request.setOffsetStart(0L);
        request.setOffsetEnd(10L);
        return request;
    }

    private UpdateSignatureRequest updateRequest() {
        UpdateSignatureRequest request = new UpdateSignatureRequest();
        request.setThreatName("Trojan.B");
        request.setFirstBytesHex("cc33");
        request.setRemainderHashHex("dd44");
        request.setRemainderLength(256);
        request.setFileType("dll");
        request.setOffsetStart(2L);
        request.setOffsetEnd(12L);
        return request;
    }

    private MalwareSignature existingSignature(UUID id, SignatureStatus status) {
        MalwareSignature signature = new MalwareSignature();
        signature.setId(id);
        signature.setThreatName("Trojan.A");
        signature.setFirstBytesHex("AA11");
        signature.setRemainderHashHex("BB22");
        signature.setRemainderLength(128);
        signature.setFileType("exe");
        signature.setOffsetStart(0L);
        signature.setOffsetEnd(10L);
        signature.setStatus(status);
        signature.setDigitalSignatureBase64(signingService.signObject(signingPayload(signature)));
        signature.setCreatedAt(LocalDateTime.now().minusDays(1));
        signature.setUpdatedAt(LocalDateTime.now());
        return signature;
    }

    private Map<String, Object> signingPayload(MalwareSignature signature) {
        return Map.of(
                "threatName", signature.getThreatName(),
                "firstBytesHex", signature.getFirstBytesHex(),
                "remainderHashHex", signature.getRemainderHashHex(),
                "remainderLength", signature.getRemainderLength(),
                "fileType", signature.getFileType(),
                "offsetStart", signature.getOffsetStart(),
                "offsetEnd", signature.getOffsetEnd(),
                "status", signature.getStatus().name()
        );
    }

    private Map<String, Object> signingPayloadFromResponse(com.rentalservice.antivirus.signature.dto.SignatureResponse response) {
        return Map.of(
                "threatName", response.getThreatName(),
                "firstBytesHex", response.getFirstBytesHex(),
                "remainderHashHex", response.getRemainderHashHex(),
                "remainderLength", response.getRemainderLength(),
                "fileType", response.getFileType(),
                "offsetStart", response.getOffsetStart(),
                "offsetEnd", response.getOffsetEnd(),
                "status", response.getStatus().name()
        );
    }
}
