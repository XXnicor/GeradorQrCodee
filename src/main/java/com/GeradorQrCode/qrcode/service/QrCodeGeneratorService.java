package com.GeradorQrCode.qrcode.service;

import com.GeradorQrCode.qrcode.dto.QrCodeGenerateResponse;
import com.GeradorQrCode.qrcode.ports.StoredFileResult;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import com.GeradorQrCode.qrcode.ports.StoragePort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class QrCodeGeneratorService {

    private StoragePort storage;

    public QrCodeGeneratorService(StoragePort storage) {
        this.storage= storage;
    }

    public QrCodeGenerateResponse generateAndUploadQrCode(String text) throws WriterException, IOException {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("O texto para geração do QR Code não pode ser nullo ou vazio");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(text, com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(bitMatrix,"PNG", pngOutputStream);

        byte[] pngBytes = pngOutputStream.toByteArray();

        String fileName = UUID.randomUUID().toString() + ".png";
        StoredFileResult storedFile = storage.uploadFile(pngBytes, fileName, "image/png");

        return new QrCodeGenerateResponse(
                storedFile.downloadUrl(),
                storedFile.expiresInSeconds(),
                storedFile.fileName(),
                storedFile.contentType()
        );
    }
}
