package com.GeradorQrCode.qrcode.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timeStamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
