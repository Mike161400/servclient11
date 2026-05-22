package com.rentalservice.antivirus.signature.controller;

import com.rentalservice.antivirus.signature.dto.CreateSignatureRequest;
import com.rentalservice.antivirus.signature.dto.SignatureAuditResponse;
import com.rentalservice.antivirus.signature.dto.SignatureHistoryResponse;
import com.rentalservice.antivirus.signature.dto.SignatureIdsRequest;
import com.rentalservice.antivirus.signature.dto.SignatureResponse;
import com.rentalservice.antivirus.signature.dto.UpdateSignatureRequest;
import com.rentalservice.antivirus.signature.service.SignatureService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/full")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<SignatureResponse> getFullDatabase() {
        return signatureService.getFullDatabase();
    }

    @GetMapping("/increment")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<SignatureResponse> getIncrementSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        return signatureService.getIncrementSince(since);
    }

    @PostMapping("/by-ids")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<SignatureResponse> getByIds(@Valid @RequestBody SignatureIdsRequest request) {
        return signatureService.getByIds(request);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SignatureResponse create(@Valid @RequestBody CreateSignatureRequest request) {
        return signatureService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SignatureResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSignatureRequest request) {
        return signatureService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SignatureResponse logicalDelete(@PathVariable UUID id) {
        return signatureService.logicalDelete(id);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SignatureHistoryResponse> getHistory(@PathVariable UUID id) {
        return signatureService.getHistory(id);
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SignatureAuditResponse> getAudit(@PathVariable UUID id) {
        return signatureService.getAudit(id);
    }
}
