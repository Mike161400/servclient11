package com.rentalservice.antivirus.signature.service;

import com.rentalservice.antivirus.eds.service.SigningService;
import com.rentalservice.antivirus.signature.dto.CreateSignatureRequest;
import com.rentalservice.antivirus.signature.dto.SignatureAuditResponse;
import com.rentalservice.antivirus.signature.dto.SignatureHistoryResponse;
import com.rentalservice.antivirus.signature.dto.SignatureIdsRequest;
import com.rentalservice.antivirus.signature.dto.SignatureResponse;
import com.rentalservice.antivirus.signature.dto.UpdateSignatureRequest;
import com.rentalservice.antivirus.signature.entity.MalwareSignature;
import com.rentalservice.antivirus.signature.entity.MalwareSignatureAudit;
import com.rentalservice.antivirus.signature.entity.MalwareSignatureHistory;
import com.rentalservice.antivirus.signature.entity.SignatureStatus;
import com.rentalservice.antivirus.signature.exception.SignatureNotFoundException;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureAuditRepository;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureHistoryRepository;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SignatureService {

    private static final List<String> SIGNED_FIELDS = List.of(
            "threatName",
            "firstBytesHex",
            "remainderHashHex",
            "remainderLength",
            "fileType",
            "offsetStart",
            "offsetEnd",
            "status"
    );

    private final MalwareSignatureRepository signatureRepository;
    private final MalwareSignatureHistoryRepository historyRepository;
    private final MalwareSignatureAuditRepository auditRepository;
    private final SigningService signingService;

    public SignatureService(MalwareSignatureRepository signatureRepository,
                            MalwareSignatureHistoryRepository historyRepository,
                            MalwareSignatureAuditRepository auditRepository,
                            SigningService signingService) {
        this.signatureRepository = signatureRepository;
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
        this.signingService = signingService;
    }

    @Transactional(readOnly = true)
    public List<SignatureResponse> getFullDatabase() {
        return signatureRepository.findAllByStatusOrderByUpdatedAtAsc(SignatureStatus.ACTUAL).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SignatureResponse> getIncrementSince(Instant since) {
        LocalDateTime threshold = LocalDateTime.ofInstant(since, ZoneId.systemDefault());
        return signatureRepository.findAllByUpdatedAtAfterOrderByUpdatedAtAsc(threshold).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SignatureResponse> getByIds(SignatureIdsRequest request) {
        List<MalwareSignature> signatures = signatureRepository.findAllById(request.getIds());
        Map<UUID, MalwareSignature> signaturesById = signatures.stream()
                .collect(Collectors.toMap(MalwareSignature::getId, signature -> signature));

        return request.getIds().stream()
                .map(signaturesById::get)
                .filter(java.util.Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    public SignatureResponse create(CreateSignatureRequest request) {
        MalwareSignature signature = new MalwareSignature();
        applyCreateOrUpdate(signature, request);
        signature.setStatus(SignatureStatus.ACTUAL);
        signature.setDigitalSignatureBase64(sign(buildSigningPayload(signature)));

        MalwareSignature saved = signatureRepository.save(signature);
        createAudit(saved.getId(), "CREATE", List.of(
                "threatName",
                "firstBytesHex",
                "remainderHashHex",
                "remainderLength",
                "fileType",
                "offsetStart",
                "offsetEnd",
                "status",
                "digitalSignatureBase64"
        ), "Created malware signature");
        return toResponse(saved);
    }

    public SignatureResponse update(UUID id, UpdateSignatureRequest request) {
        MalwareSignature existing = signatureRepository.findById(id)
                .orElseThrow(() -> new SignatureNotFoundException(id));

        saveHistory(existing);

        List<String> changedFields = computeChangedFields(existing, request);
        applyCreateOrUpdate(existing, request);
        String oldSignature = existing.getDigitalSignatureBase64();
        existing.setDigitalSignatureBase64(sign(buildSigningPayload(existing)));

        if (!java.util.Objects.equals(oldSignature, existing.getDigitalSignatureBase64())) {
            changedFields.add("digitalSignatureBase64");
        }

        MalwareSignature saved = signatureRepository.save(existing);
        createAudit(saved.getId(), "UPDATE", distinctSorted(changedFields), "Updated malware signature");
        return toResponse(saved);
    }

    public SignatureResponse logicalDelete(UUID id) {
        MalwareSignature existing = signatureRepository.findById(id)
                .orElseThrow(() -> new SignatureNotFoundException(id));

        if (existing.getStatus() == SignatureStatus.DELETED) {
            return toResponse(existing);
        }

        saveHistory(existing);
        existing.setStatus(SignatureStatus.DELETED);
        existing.setDigitalSignatureBase64(sign(buildSigningPayload(existing)));

        MalwareSignature saved = signatureRepository.save(existing);
        createAudit(saved.getId(), "DELETE", List.of("status", "digitalSignatureBase64"), "Logically deleted malware signature");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SignatureHistoryResponse> getHistory(UUID signatureId) {
        ensureSignatureExists(signatureId);
        return historyRepository.findAllBySignatureIdOrderByRecordedAtDesc(signatureId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SignatureAuditResponse> getAudit(UUID signatureId) {
        ensureSignatureExists(signatureId);
        return auditRepository.findAllBySignatureIdOrderByChangedAtDesc(signatureId).stream()
                .map(this::toAuditResponse)
                .toList();
    }

    private void ensureSignatureExists(UUID signatureId) {
        if (!signatureRepository.existsById(signatureId)) {
            throw new SignatureNotFoundException(signatureId);
        }
    }

    private void applyCreateOrUpdate(MalwareSignature signature, CreateSignatureRequest request) {
        signature.setThreatName(normalizeText(request.getThreatName()));
        signature.setFirstBytesHex(normalizeHex(request.getFirstBytesHex()));
        signature.setRemainderHashHex(normalizeHex(request.getRemainderHashHex()));
        signature.setRemainderLength(request.getRemainderLength());
        signature.setFileType(normalizeText(request.getFileType()));
        signature.setOffsetStart(request.getOffsetStart());
        signature.setOffsetEnd(request.getOffsetEnd());
    }

    private void applyCreateOrUpdate(MalwareSignature signature, UpdateSignatureRequest request) {
        signature.setThreatName(normalizeText(request.getThreatName()));
        signature.setFirstBytesHex(normalizeHex(request.getFirstBytesHex()));
        signature.setRemainderHashHex(normalizeHex(request.getRemainderHashHex()));
        signature.setRemainderLength(request.getRemainderLength());
        signature.setFileType(normalizeText(request.getFileType()));
        signature.setOffsetStart(request.getOffsetStart());
        signature.setOffsetEnd(request.getOffsetEnd());
    }

    private void saveHistory(MalwareSignature signature) {
        MalwareSignatureHistory history = new MalwareSignatureHistory();
        history.setSignatureId(signature.getId());
        history.setThreatName(signature.getThreatName());
        history.setFirstBytesHex(signature.getFirstBytesHex());
        history.setRemainderHashHex(signature.getRemainderHashHex());
        history.setRemainderLength(signature.getRemainderLength());
        history.setFileType(signature.getFileType());
        history.setOffsetStart(signature.getOffsetStart());
        history.setOffsetEnd(signature.getOffsetEnd());
        history.setStatus(signature.getStatus());
        history.setDigitalSignatureBase64(signature.getDigitalSignatureBase64());
        history.setOriginalCreatedAt(signature.getCreatedAt());
        history.setOriginalUpdatedAt(signature.getUpdatedAt());
        historyRepository.save(history);
    }

    private void createAudit(UUID signatureId, String action, List<String> fieldsChanged, String description) {
        MalwareSignatureAudit audit = new MalwareSignatureAudit();
        audit.setSignatureId(signatureId);
        audit.setAction(action);
        audit.setChangedBy(currentActor());
        audit.setFieldsChanged(String.join(",", distinctSorted(fieldsChanged)));
        audit.setDescription(description);
        auditRepository.save(audit);
    }

    private List<String> computeChangedFields(MalwareSignature existing, UpdateSignatureRequest request) {
        List<String> changedFields = new ArrayList<>();
        if (!java.util.Objects.equals(existing.getThreatName(), normalizeText(request.getThreatName()))) {
            changedFields.add("threatName");
        }
        if (!java.util.Objects.equals(existing.getFirstBytesHex(), normalizeHex(request.getFirstBytesHex()))) {
            changedFields.add("firstBytesHex");
        }
        if (!java.util.Objects.equals(existing.getRemainderHashHex(), normalizeHex(request.getRemainderHashHex()))) {
            changedFields.add("remainderHashHex");
        }
        if (!java.util.Objects.equals(existing.getRemainderLength(), request.getRemainderLength())) {
            changedFields.add("remainderLength");
        }
        if (!java.util.Objects.equals(existing.getFileType(), normalizeText(request.getFileType()))) {
            changedFields.add("fileType");
        }
        if (!java.util.Objects.equals(existing.getOffsetStart(), request.getOffsetStart())) {
            changedFields.add("offsetStart");
        }
        if (!java.util.Objects.equals(existing.getOffsetEnd(), request.getOffsetEnd())) {
            changedFields.add("offsetEnd");
        }
        return changedFields;
    }

    private SignatureResponse toResponse(MalwareSignature signature) {
        SignatureResponse response = new SignatureResponse();
        response.setId(signature.getId());
        response.setThreatName(signature.getThreatName());
        response.setFirstBytesHex(signature.getFirstBytesHex());
        response.setRemainderHashHex(signature.getRemainderHashHex());
        response.setRemainderLength(signature.getRemainderLength());
        response.setFileType(signature.getFileType());
        response.setOffsetStart(signature.getOffsetStart());
        response.setOffsetEnd(signature.getOffsetEnd());
        response.setUpdatedAt(signature.getUpdatedAt());
        response.setStatus(signature.getStatus());
        response.setDigitalSignatureBase64(signature.getDigitalSignatureBase64());
        return response;
    }

    private SignatureHistoryResponse toHistoryResponse(MalwareSignatureHistory history) {
        SignatureHistoryResponse response = new SignatureHistoryResponse();
        response.setId(history.getId());
        response.setSignatureId(history.getSignatureId());
        response.setThreatName(history.getThreatName());
        response.setFirstBytesHex(history.getFirstBytesHex());
        response.setRemainderHashHex(history.getRemainderHashHex());
        response.setRemainderLength(history.getRemainderLength());
        response.setFileType(history.getFileType());
        response.setOffsetStart(history.getOffsetStart());
        response.setOffsetEnd(history.getOffsetEnd());
        response.setStatus(history.getStatus());
        response.setDigitalSignatureBase64(history.getDigitalSignatureBase64());
        response.setOriginalCreatedAt(history.getOriginalCreatedAt());
        response.setOriginalUpdatedAt(history.getOriginalUpdatedAt());
        response.setRecordedAt(history.getRecordedAt());
        return response;
    }

    private SignatureAuditResponse toAuditResponse(MalwareSignatureAudit audit) {
        SignatureAuditResponse response = new SignatureAuditResponse();
        response.setId(audit.getId());
        response.setSignatureId(audit.getSignatureId());
        response.setAction(audit.getAction());
        response.setChangedBy(audit.getChangedBy());
        response.setChangedAt(audit.getChangedAt());
        response.setFieldsChanged(parseFieldsChanged(audit.getFieldsChanged()));
        response.setDescription(audit.getDescription());
        return response;
    }

    private String sign(Object payload) {
        return signingService.signObject(payload);
    }

    private Map<String, Object> buildSigningPayload(MalwareSignature signature) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("threatName", signature.getThreatName());
        payload.put("firstBytesHex", signature.getFirstBytesHex());
        payload.put("remainderHashHex", signature.getRemainderHashHex());
        payload.put("remainderLength", signature.getRemainderLength());
        payload.put("fileType", signature.getFileType());
        payload.put("offsetStart", signature.getOffsetStart());
        payload.put("offsetEnd", signature.getOffsetEnd());
        payload.put("status", signature.getStatus().name());
        return payload;
    }

    private List<String> parseFieldsChanged(String fieldsChanged) {
        if (fieldsChanged == null || fieldsChanged.isBlank()) {
            return List.of();
        }
        return Arrays.stream(fieldsChanged.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private List<String> distinctSorted(Collection<String> fields) {
        return fields.stream()
                .distinct()
                .sorted()
                .toList();
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeHex(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}
