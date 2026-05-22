package com.rentalservice.antivirus.binary.service;

import com.rentalservice.antivirus.binary.controller.BinarySignatureController;
import com.rentalservice.antivirus.binary.model.BinaryExportResult;
import com.rentalservice.antivirus.eds.properties.EdsProperties;
import com.rentalservice.antivirus.eds.service.CanonicalizationService;
import com.rentalservice.antivirus.eds.service.KeyProvider;
import com.rentalservice.antivirus.eds.service.SigningService;
import com.rentalservice.antivirus.signature.dto.SignatureIdsRequest;
import com.rentalservice.antivirus.signature.dto.SignatureResponse;
import com.rentalservice.antivirus.signature.entity.SignatureStatus;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureAuditRepository;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureHistoryRepository;
import com.rentalservice.antivirus.signature.repository.MalwareSignatureRepository;
import com.rentalservice.antivirus.signature.service.SignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BinaryExportServiceTest {

    private SigningService signingService;
    private CountingSigningService countingSigningService;
    private StubSignatureService stubSignatureService;
    private BinaryExportService binaryExportService;

    @BeforeEach
    void setUp() {
        EdsProperties properties = new EdsProperties();
        properties.setKeyStorePath("classpath:eds/test-keystore.p12");
        properties.setKeyStoreType("PKCS12");
        properties.setKeyStorePassword("changeit");
        properties.setKeyAlias("antivirus-test");
        properties.setKeyPassword("changeit");
        properties.setAlgorithm("SHA256withRSA");

        KeyProvider keyProvider = new KeyProvider(properties, new DefaultResourceLoader());
        CanonicalizationService canonicalizationService = new CanonicalizationService();
        signingService = new SigningService(keyProvider, canonicalizationService, properties);
        countingSigningService = new CountingSigningService(keyProvider, canonicalizationService, properties);
        stubSignatureService = new StubSignatureService(signingService);
        binaryExportService = new BinaryExportService(stubSignatureService, countingSigningService);
    }

    @Test
    void fullBinaryExportContainsMultipartWithManifestAndData() throws Exception {
        SignatureResponse actual = buildSignature("Trojan.Full", SignatureStatus.ACTUAL, LocalDateTime.now());
        SignatureResponse deleted = buildSignature("Trojan.Deleted", SignatureStatus.DELETED, LocalDateTime.now());
        stubSignatureService.fullResults = List.of(actual);
        stubSignatureService.incrementResults = List.of(actual, deleted);

        BinarySignatureController controller = new BinarySignatureController(binaryExportService);
        var response = controller.exportFull();

        assertEquals(MediaType.MULTIPART_MIXED, response.getHeaders().getContentType());
        MultiValueMap<String, HttpEntity<?>> body = response.getBody();
        assertEquals(List.of("manifest.bin", "data.bin"), new ArrayList<>(body.keySet()));
        assertTrue(body.getFirst("manifest.bin").getBody() instanceof ByteArrayResource);
        assertTrue(body.getFirst("data.bin").getBody() instanceof ByteArrayResource);
    }

    @Test
    void fullDoesNotContainDeleted() throws Exception {
        SignatureResponse actual = buildSignature("Trojan.Full", SignatureStatus.ACTUAL, LocalDateTime.now());
        stubSignatureService.fullResults = List.of(actual);

        BinaryExportResult result = binaryExportService.exportFull();
        ParsedManifest manifest = parseManifest(result.manifestBytes());

        assertEquals(1, manifest.recordCount);
        assertEquals(1, manifest.entries.size());
        assertEquals(1, manifest.entries.get(0).statusCode);
        ParsedData parsedData = parseData(result.dataBytes());
        assertEquals(1, parsedData.recordCount);
    }

    @Test
    void incrementContainsDeleted() throws Exception {
        SignatureResponse actual = buildSignature("Trojan.Full", SignatureStatus.ACTUAL, LocalDateTime.now());
        SignatureResponse deleted = buildSignature("Trojan.Deleted", SignatureStatus.DELETED, LocalDateTime.now().plusSeconds(1));
        stubSignatureService.incrementResults = List.of(actual, deleted);

        BinaryExportResult result = binaryExportService.exportIncrement(Instant.parse("2026-05-22T10:00:00Z"));
        ParsedManifest manifest = parseManifest(result.manifestBytes());

        assertEquals(2, manifest.recordCount);
        assertTrue(manifest.entries.stream().anyMatch(entry -> entry.statusCode == 2));
        ParsedData parsedData = parseData(result.dataBytes());
        assertEquals(2, parsedData.recordCount);
    }

    @Test
    void manifestIsSignedThroughSignBytesAndSha256Matches() throws Exception {
        SignatureResponse actual = buildSignature("Trojan.Full", SignatureStatus.ACTUAL, LocalDateTime.now());
        stubSignatureService.fullResults = List.of(actual);

        BinaryExportResult result = binaryExportService.exportFull();
        ParsedManifest manifest = parseManifest(result.manifestBytes());

        assertTrue(countingSigningService.signBytesInvocations > 0);
        assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(result.dataBytes()), manifest.dataSha256);

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(countingSigningService.getPublicKey());
        verifier.update(manifest.unsignedBytes);
        assertTrue(verifier.verify(manifest.manifestSignature));
    }

    @Test
    void offsetsAndLengthsAreCorrect() throws Exception {
        SignatureResponse first = buildSignature("Trojan.One", SignatureStatus.ACTUAL, LocalDateTime.now());
        SignatureResponse second = buildSignature("Trojan.Two", SignatureStatus.ACTUAL, LocalDateTime.now().plusSeconds(1));
        stubSignatureService.fullResults = List.of(first, second);

        BinaryExportResult result = binaryExportService.exportFull();
        ParsedManifest manifest = parseManifest(result.manifestBytes());
        ParsedData data = parseData(result.dataBytes());

        assertEquals(2, manifest.entries.size());
        assertEquals(2, data.records.size());
        for (int i = 0; i < manifest.entries.size(); i++) {
            ManifestEntry entry = manifest.entries.get(i);
            DataRecord record = data.records.get(i);
            assertEquals(record.offset, entry.dataOffset);
            assertEquals(record.length, entry.dataLength);
        }
    }

    private SignatureResponse buildSignature(String threatName, SignatureStatus status, LocalDateTime updatedAt) {
        SignatureResponse response = new SignatureResponse();
        response.setId(UUID.randomUUID());
        response.setThreatName(threatName);
        response.setFirstBytesHex("4D5A9000");
        response.setRemainderHashHex("A1B2C3D4");
        response.setRemainderLength(1024);
        response.setFileType("exe");
        response.setOffsetStart(0L);
        response.setOffsetEnd(128L);
        response.setUpdatedAt(updatedAt);
        response.setStatus(status);
        response.setDigitalSignatureBase64(signingService.signObject(Map.of(
                "threatName", threatName,
                "firstBytesHex", "4D5A9000",
                "remainderHashHex", "A1B2C3D4",
                "remainderLength", 1024,
                "fileType", "exe",
                "offsetStart", 0L,
                "offsetEnd", 128L,
                "status", status.name()
        )));
        return response;
    }

    private ParsedManifest parseManifest(byte[] manifestBytes) throws Exception {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(manifestBytes))) {
            byte[] magic = input.readNBytes(8);
            assertEquals("MF-RUDIK", new String(magic, StandardCharsets.US_ASCII));
            int version = input.readUnsignedShort();
            assertEquals(1, version);
            int exportType = input.readUnsignedByte();
            long generatedAt = input.readLong();
            long sinceEpochMillis = input.readLong();
            long recordCount = Integer.toUnsignedLong(input.readInt());
            byte[] dataSha256 = input.readNBytes(32);

            List<ManifestEntry> entries = new ArrayList<>();
            for (int i = 0; i < recordCount; i++) {
                UUID id = new UUID(input.readLong(), input.readLong());
                int statusCode = input.readUnsignedByte();
                long updatedAtEpochMillis = input.readLong();
                long dataOffset = Integer.toUnsignedLong(input.readInt());
                long dataLength = Integer.toUnsignedLong(input.readInt());
                long recordSignatureLength = Integer.toUnsignedLong(input.readInt());
                byte[] recordSignatureBytes = input.readNBytes((int) recordSignatureLength);
                entries.add(new ManifestEntry(id, statusCode, updatedAtEpochMillis, dataOffset, dataLength, recordSignatureBytes));
            }

            int unsignedLength = manifestBytes.length - input.available();
            long manifestSignatureLength = Integer.toUnsignedLong(input.readInt());
            byte[] manifestSignature = input.readNBytes((int) manifestSignatureLength);
            byte[] unsignedBytes = java.util.Arrays.copyOf(manifestBytes, unsignedLength);

            return new ParsedManifest(exportType, generatedAt, sinceEpochMillis, (int) recordCount,
                    dataSha256, entries, unsignedBytes, manifestSignature);
        }
    }

    private ParsedData parseData(byte[] dataBytes) throws Exception {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(dataBytes))) {
            byte[] magic = input.readNBytes(8);
            assertEquals("DB-RUDIK", new String(magic, StandardCharsets.US_ASCII));
            int version = input.readUnsignedShort();
            assertEquals(1, version);
            int recordCount = input.readInt();

            List<DataRecord> records = new ArrayList<>();
            while (input.available() > 0) {
                int recordOffset = dataBytes.length - input.available();
                String threatName = readString(input);
                byte[] firstBytes = readBytes(input);
                byte[] remainderHash = readBytes(input);
                long remainderLength = Integer.toUnsignedLong(input.readInt());
                String fileType = readString(input);
                long offsetStart = input.readLong();
                long offsetEnd = input.readLong();
                int recordEnd = dataBytes.length - input.available();
                records.add(new DataRecord(recordOffset, recordEnd - recordOffset, threatName, firstBytes,
                        remainderHash, remainderLength, fileType, offsetStart, offsetEnd));
            }

            return new ParsedData(recordCount, records);
        }
    }

    private String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        return new String(input.readNBytes(length), StandardCharsets.UTF_8);
    }

    private byte[] readBytes(DataInputStream input) throws IOException {
        int length = input.readInt();
        return input.readNBytes(length);
    }

    private static class CountingSigningService extends SigningService {
        private final PublicKey publicKey;
        private int signBytesInvocations;

        CountingSigningService(KeyProvider keyProvider,
                               CanonicalizationService canonicalizationService,
                               EdsProperties edsProperties) {
            super(keyProvider, canonicalizationService, edsProperties);
            this.publicKey = keyProvider.getPublicKey();
        }

        @Override
        public byte[] signBytes(byte[] payload) {
            signBytesInvocations++;
            return super.signBytes(payload);
        }

        PublicKey getPublicKey() {
            return publicKey;
        }
    }

    private static class StubSignatureService extends SignatureService {
        private List<SignatureResponse> fullResults = List.of();
        private List<SignatureResponse> incrementResults = List.of();
        private List<SignatureResponse> byIdsResults = List.of();

        StubSignatureService(SigningService signingService) {
            super(
                    mock(MalwareSignatureRepository.class),
                    mock(MalwareSignatureHistoryRepository.class),
                    mock(MalwareSignatureAuditRepository.class),
                    signingService
            );
        }

        @Override
        public List<SignatureResponse> getFullDatabase() {
            return fullResults;
        }

        @Override
        public List<SignatureResponse> getIncrementSince(Instant since) {
            return incrementResults;
        }

        @Override
        public List<SignatureResponse> getByIds(SignatureIdsRequest request) {
            return byIdsResults.isEmpty() ? fullResults : byIdsResults;
        }
    }

    private record ManifestEntry(UUID id,
                                 int statusCode,
                                 long updatedAtEpochMillis,
                                 long dataOffset,
                                 long dataLength,
                                 byte[] recordSignatureBytes) {
    }

    private record ParsedManifest(int exportType,
                                  long generatedAt,
                                  long sinceEpochMillis,
                                  int recordCount,
                                  byte[] dataSha256,
                                  List<ManifestEntry> entries,
                                  byte[] unsignedBytes,
                                  byte[] manifestSignature) {
    }

    private record DataRecord(int offset,
                              int length,
                              String threatName,
                              byte[] firstBytes,
                              byte[] remainderHash,
                              long remainderLength,
                              String fileType,
                              long offsetStart,
                              long offsetEnd) {
    }

    private record ParsedData(int recordCount, List<DataRecord> records) {
    }
}
