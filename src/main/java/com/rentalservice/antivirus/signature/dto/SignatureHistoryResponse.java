package com.rentalservice.antivirus.signature.dto;

import com.rentalservice.antivirus.signature.entity.SignatureStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class SignatureHistoryResponse {

    private UUID id;
    private UUID signatureId;
    private String threatName;
    private String firstBytesHex;
    private String remainderHashHex;
    private Integer remainderLength;
    private String fileType;
    private Long offsetStart;
    private Long offsetEnd;
    private SignatureStatus status;
    private String digitalSignatureBase64;
    private LocalDateTime originalCreatedAt;
    private LocalDateTime originalUpdatedAt;
    private LocalDateTime recordedAt;

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

    public String getThreatName() {
        return threatName;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public String getFirstBytesHex() {
        return firstBytesHex;
    }

    public void setFirstBytesHex(String firstBytesHex) {
        this.firstBytesHex = firstBytesHex;
    }

    public String getRemainderHashHex() {
        return remainderHashHex;
    }

    public void setRemainderHashHex(String remainderHashHex) {
        this.remainderHashHex = remainderHashHex;
    }

    public Integer getRemainderLength() {
        return remainderLength;
    }

    public void setRemainderLength(Integer remainderLength) {
        this.remainderLength = remainderLength;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(Long offsetStart) {
        this.offsetStart = offsetStart;
    }

    public Long getOffsetEnd() {
        return offsetEnd;
    }

    public void setOffsetEnd(Long offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public SignatureStatus getStatus() {
        return status;
    }

    public void setStatus(SignatureStatus status) {
        this.status = status;
    }

    public String getDigitalSignatureBase64() {
        return digitalSignatureBase64;
    }

    public void setDigitalSignatureBase64(String digitalSignatureBase64) {
        this.digitalSignatureBase64 = digitalSignatureBase64;
    }

    public LocalDateTime getOriginalCreatedAt() {
        return originalCreatedAt;
    }

    public void setOriginalCreatedAt(LocalDateTime originalCreatedAt) {
        this.originalCreatedAt = originalCreatedAt;
    }

    public LocalDateTime getOriginalUpdatedAt() {
        return originalUpdatedAt;
    }

    public void setOriginalUpdatedAt(LocalDateTime originalUpdatedAt) {
        this.originalUpdatedAt = originalUpdatedAt;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
