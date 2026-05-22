package com.rentalservice.antivirus.binary.util;

import com.rentalservice.antivirus.binary.exception.BinaryExportException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryWriter {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

    public void writeU8(int value) {
        if (value < 0 || value > 0xFF) {
            throw new BinaryExportException("Value out of range for U8: " + value);
        }
        try {
            dataOutputStream.writeByte(value);
        } catch (IOException ex) {
            throw new BinaryExportException("Failed to write U8 value", ex);
        }
    }

    public void writeU16(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new BinaryExportException("Value out of range for U16: " + value);
        }
        try {
            dataOutputStream.writeShort(value);
        } catch (IOException ex) {
            throw new BinaryExportException("Failed to write U16 value", ex);
        }
    }

    public void writeU32(long value) {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new BinaryExportException("Value out of range for U32: " + value);
        }
        try {
            dataOutputStream.writeInt((int) value);
        } catch (IOException ex) {
            throw new BinaryExportException("Failed to write U32 value", ex);
        }
    }

    public void writeI64(long value) {
        try {
            dataOutputStream.writeLong(value);
        } catch (IOException ex) {
            throw new BinaryExportException("Failed to write I64 value", ex);
        }
    }

    public void writeUUID(UUID value) {
        writeI64(value.getMostSignificantBits());
        writeI64(value.getLeastSignificantBits());
    }

    public void writeStringUtf8(String value) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        writeU32(bytes.length);
        writeRawBytes(bytes);
    }

    public void writeBytes(byte[] value) {
        byte[] bytes = value == null ? new byte[0] : value;
        writeU32(bytes.length);
        writeRawBytes(bytes);
    }

    public void writeAscii(String value) {
        writeRawBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    public void writeRawBytes(byte[] value) {
        try {
            dataOutputStream.write(value);
        } catch (IOException ex) {
            throw new BinaryExportException("Failed to write raw bytes", ex);
        }
    }

    public int size() {
        return outputStream.size();
    }

    public byte[] toByteArray() {
        try {
            dataOutputStream.flush();
        } catch (IOException ex) {
            throw new BinaryExportException("Failed to flush binary writer", ex);
        }
        return outputStream.toByteArray();
    }
}
