package com.rentalservice.antivirus.signature.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateSignatureRequest {

    @NotBlank(message = "threatName must not be blank")
    private String threatName;

    @NotBlank(message = "firstBytesHex must not be blank")
    @Pattern(regexp = "^[0-9A-Fa-f]+$", message = "firstBytesHex must contain only hex characters")
    private String firstBytesHex;

    @NotBlank(message = "remainderHashHex must not be blank")
    @Pattern(regexp = "^[0-9A-Fa-f]+$", message = "remainderHashHex must contain only hex characters")
    private String remainderHashHex;

    @NotNull(message = "remainderLength is required")
    @Min(value = 0, message = "remainderLength must be >= 0")
    private Integer remainderLength;

    @NotBlank(message = "fileType must not be blank")
    private String fileType;

    @NotNull(message = "offsetStart is required")
    @Min(value = 0, message = "offsetStart must be >= 0")
    private Long offsetStart;

    @NotNull(message = "offsetEnd is required")
    @Min(value = 0, message = "offsetEnd must be >= 0")
    private Long offsetEnd;

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

    @AssertTrue(message = "offsetEnd must be greater than or equal to offsetStart")
    public boolean isOffsetRangeValid() {
        return offsetStart == null || offsetEnd == null || offsetEnd >= offsetStart;
    }
}
