package com.GeradorQrCode.qrcode.ports;

public record StoredFileResult(
        String downloadUrl,
        long expiresInSeconds,
        String fileName,
        String contentType
) {
}
