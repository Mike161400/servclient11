package com.rentalservice.antivirus.binary.service;

import com.rentalservice.antivirus.binary.exception.BinaryExportException;
import com.rentalservice.antivirus.binary.model.BinaryExportResult;
import com.rentalservice.antivirus.binary.model.BinaryExportType;
import com.rentalservice.antivirus.binary.util.BinaryWriter;
import com.rentalservice.antivirus.eds.service.SigningService;
import com.rentalservice.antivirus.signature.dto.SignatureIdsRequest;
import com.rentalservice.antivirus.signature.dto.SignatureResponse;
import com.rentalservice.antivirus.signature.entity.SignatureStatus;
import com.rentalservice.antivirus.signature.service.SignatureService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class BinaryExportService {

    private static final String DATA_MAGIC = "DB-RUDIK";
    private static final String MANIFEST_MAGIC = "MF-RUDIK";
    private static final int VERSION = 1;
    private static final long SINCE_NOT_APPLICABLE = -1L;

    private final SignatureService signatureService;
    private final SigningService signingService;

    public BinaryExportService(SignatureService signatureService, SigningService signingService) {
        this.signatureService = signatureService;
        this.signingService = signingService;
    }

    public BinaryExportResult exportFull() {
        List<SignatureResponse> signatures = signatureService.getFullDatabase();
        return export(signatures, BinaryExportType.FULL, null);
    }

    public BinaryExportResult exportIncrement(Instant since) {
        List<SignatureResponse> signatures = signatureService.getIncrementSince(since);
        return export(signatures, BinaryExportType.INCREMENT, since);
    }

    public BinaryExportResult exportByIds(SignatureIdsRequest request) {
        List<SignatureResponse> signatures = signatureService.getByIds(request);
        return export(signatures, BinaryExportType.BY_IDS, null);
    }

    private BinaryExportResult export(List<SignatureResponse> signatures, BinaryExportType exportType, Instant since) {
        DataExport dataExport = buildDataBytes(signatures);
        byte[] manifestBytes = buildManifestBytes(signatures, dataExport, exportType, since);
        return new BinaryExportResult(manifestBytes, dataExport.dataBytes());
    }

    private DataExport buildDataBytes(List<SignatureResponse> signatures) {
        BinaryWriter writer = new BinaryWriter();
        writer.writeAscii(DATA_MAGIC);
        writer.writeU16(VERSION);
        writer.writeU32(signatures.size());

        List<RecordMetadata> recordMetadata = new ArrayList<>();
        for (SignatureResponse signature : signatures) {
            int recordOffset = writer.size();
            byte[] recordBytes = buildRecordBytes(signature);
            writer.writeRawBytes(recordBytes);
            recordMetadata.add(new RecordMetadata(recordOffset, recordBytes.length));
        }

        return new DataExport(writer.toByteArray(), recordMetadata);
    }

    private byte[] buildRecordBytes(SignatureResponse signature) {
        BinaryWriter writer = new BinaryWriter();
        writer.writeStringUtf8(signature.getThreatName());
        writer.writeBytes(decodeHex(signature.getFirstBytesHex(), "firstBytesHex", signature.getId()));
        writer.writeBytes(decodeHex(signature.getRemainderHashHex(), "remainderHashHex", signature.getId()));
        writer.writeU32(signature.getRemainderLength());
        writer.writeStringUtf8(signature.getFileType());
        writer.writeI64(signature.getOffsetStart());
        writer.writeI64(signature.getOffsetEnd());
        return writer.toByteArray();
    }

    private byte[] buildManifestBytes(List<SignatureResponse> signatures,
                                      DataExport dataExport,
                                      BinaryExportType exportType,
                                      Instant since) {
        byte[] dataSha256 = sha256(dataExport.dataBytes());

        BinaryWriter writer = new BinaryWriter();
        writer.writeAscii(MANIFEST_MAGIC);
        writer.writeU16(VERSION);
        writer.writeU8(exportType.getCode());
        writer.writeI64(Instant.now().toEpochMilli());
        writer.writeI64(since == null ? SINCE_NOT_APPLICABLE : since.toEpochMilli());
        writer.writeU32(signatures.size());
        writer.writeRawBytes(dataSha256);

        for (int i = 0; i < signatures.size(); i++) {
            SignatureResponse signature = signatures.get(i);
            RecordMetadata recordMetadata = dataExport.recordMetadata().get(i);
            byte[] recordSignatureBytes = decodeBase64(signature.getDigitalSignatureBase64(), signature.getId());

            writer.writeUUID(signature.getId());
            writer.writeU8(statusCode(signature.getStatus()));
            writer.writeI64(signature.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
            writer.writeU32(recordMetadata.offset());
            writer.writeU32(recordMetadata.length());
            writer.writeU32(recordSignatureBytes.length);
            writer.writeRawBytes(recordSignatureBytes);
        }

        byte[] unsignedManifest = writer.toByteArray();
        byte[] manifestSignature = signingService.signBytes(unsignedManifest);

        BinaryWriter signedWriter = new BinaryWriter();
        signedWriter.writeRawBytes(unsignedManifest);
        signedWriter.writeU32(manifestSignature.length);
        signedWriter.writeRawBytes(manifestSignature);
        return signedWriter.toByteArray();
    }

    private int statusCode(SignatureStatus status) {
        return status == SignatureStatus.DELETED ? 2 : 1;
    }

    private byte[] decodeBase64(String base64, java.util.UUID signatureId) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException ex) {
            throw new BinaryExportException(
                    "Stored digitalSignatureBase64 is invalid for signature " + signatureId,
                    ex
            );
        }
    }

    private byte[] decodeHex(String hex, String fieldName, java.util.UUID signatureId) {
        if (hex == null) {
            return new byte[0];
        }
        String normalized = hex.trim();
        if ((normalized.length() & 1) == 1) {
            throw new BinaryExportException(
                    "Field '" + fieldName + "' must contain an even number of hex characters for signature " + signatureId
            );
        }

        byte[] result = new byte[normalized.length() / 2];
        for (int i = 0; i < normalized.length(); i += 2) {
            int high = Character.digit(normalized.charAt(i), 16);
            int low = Character.digit(normalized.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                throw new BinaryExportException(
                        "Field '" + fieldName + "' contains invalid hex characters for signature " + signatureId
                );
            }
            result[i / 2] = (byte) ((high << 4) | low);
        }
        return result;
    }

    private byte[] sha256(byte[] payload) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(payload);
        } catch (NoSuchAlgorithmException ex) {
            throw new BinaryExportException("SHA-256 algorithm is not available", ex);
        }
    }

    private record RecordMetadata(int offset, int length) {
    }

    private record DataExport(byte[] dataBytes, List<RecordMetadata> recordMetadata) {
    }
}
