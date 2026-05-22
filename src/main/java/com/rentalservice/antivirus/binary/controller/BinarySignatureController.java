package com.rentalservice.antivirus.binary.controller;

import com.rentalservice.antivirus.binary.model.BinaryExportResult;
import com.rentalservice.antivirus.binary.service.BinaryExportService;
import com.rentalservice.antivirus.signature.dto.SignatureIdsRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/binary/signatures")
public class BinarySignatureController {

    private final BinaryExportService binaryExportService;

    public BinarySignatureController(BinaryExportService binaryExportService) {
        this.binaryExportService = binaryExportService;
    }

    @GetMapping("/full")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> exportFull() {
        return toMultipartResponse(binaryExportService.exportFull());
    }

    @GetMapping("/increment")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> exportIncrement(@RequestParam Instant since) {
        return toMultipartResponse(binaryExportService.exportIncrement(since));
    }

    @PostMapping("/by-ids")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> exportByIds(@Valid @RequestBody SignatureIdsRequest request) {
        return toMultipartResponse(binaryExportService.exportByIds(request));
    }

    private ResponseEntity<MultiValueMap<String, HttpEntity<?>>> toMultipartResponse(BinaryExportResult exportResult) {
        LinkedMultiValueMap<String, HttpEntity<?>> body = new LinkedMultiValueMap<>();
        body.add("manifest.bin", buildPart("manifest.bin", exportResult.manifestBytes()));
        body.add("data.bin", buildPart("data.bin", exportResult.dataBytes()));

        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(body);
    }

    private HttpEntity<ByteArrayResource> buildPart(String fileName, byte[] content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .name(fileName)
                .filename(fileName)
                .build());

        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        return new HttpEntity<>(resource, headers);
    }
}
