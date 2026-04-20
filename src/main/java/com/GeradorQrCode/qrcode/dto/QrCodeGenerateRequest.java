package com.GeradorQrCode.qrcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QrCodeGenerateRequest(
        @NotBlank(message = "O texto é obrigatorio")
        @Size(max = 500, message = "O texto não pode ultrapassar 500 caracteres")
        String text
) {
}
