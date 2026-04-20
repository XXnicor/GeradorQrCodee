package com.GeradorQrCode.qrcode.controller;

import com.GeradorQrCode.qrcode.dto.QrCodeGenerateResponse;
import com.GeradorQrCode.qrcode.dto.QrCodeGenerateRequest;
import com.GeradorQrCode.qrcode.service.QrCodeGeneratorService;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/qrcode")
public class QrCodeController {

    private final QrCodeGeneratorService qrCodeGeneratorService;

    public QrCodeController(QrCodeGeneratorService qrCodeGeneratorService) {
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }

    @PostMapping
    public ResponseEntity<QrCodeGenerateResponse> generate(@Valid @RequestBody QrCodeGenerateRequest request) throws IOException, WriterException {
            QrCodeGenerateResponse response = qrCodeGeneratorService.generateAndUploadQrCode(request.text());
            return ResponseEntity.ok(response);
    }
}
