package com.rentalservice.antivirus.signature.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SignatureAuditResponse {

    private UUID id;
    private UUID signatureId;
    private String action;
    private String changedBy;
    private LocalDateTime changedAt;
    private List<String> fieldsChanged;
    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSignatureId() {
        return signatureId;
    }

    public void setSignatureId(UUID signatureId) {
        this.signatureId = signatureId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public List<String> getFieldsChanged() {
        return fieldsChanged;
    }

    public void setFieldsChanged(List<String> fieldsChanged) {
        this.fieldsChanged = fieldsChanged;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
