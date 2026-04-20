package com.GeradorQrCode.qrcode.ports;

public interface StoragePort {

    StoredFileResult uploadFile(byte[] data, String fileName, String contentType);
}
