package com.GeradorQrCode.qrcode.dto;

public record QrCodeGenerateResponse(String downloadUrl,
                                     long expiresInSeconds,
                                     String fileName,
                                     String contentType) {

}
