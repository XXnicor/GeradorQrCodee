package com.GeradorQrCode.qrcode.service;


import com.GeradorQrCode.qrcode.dto.QrCodeGenerateResponse;
import com.GeradorQrCode.qrcode.ports.StoragePort;
import com.GeradorQrCode.qrcode.ports.StoredFileResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrCodeGeneratorServiceTest {

    @Mock
    private StoragePort storage;

    @InjectMocks
    private QrCodeGeneratorService qrCodeGeneratorService;

    @Test
    @DisplayName("Deve gerar QR Code e retornar a URL do S3 com sucesso")
    void deveGerarQrCodeERetornarUrl() throws Exception {
        // Dado que o storage retornar uma URL ficticia
        String textoEntrada = "https://github.com/meu-usuario";

        when(storage.uploadFile(any(byte[].class), any(String.class), eq("image/png")))
                .thenReturn(new StoredFileResult(
                        "https://s3.amazonaws.com/meu-bucket/qrcode-123.png?signature=abc",
                        900L,
                        "qrcode-123.png",
                        "image/png"
                ));

        // Quando o serviço for chamado
        QrCodeGenerateResponse response = qrCodeGeneratorService.generateAndUploadQrCode(textoEntrada);

        assertThat(response.downloadUrl()).isEqualTo("https://s3.amazonaws.com/meu-bucket/qrcode-123.png?signature=abc");
        assertThat(response.expiresInSeconds()).isEqualTo(900L);
        assertThat(response.fileName()).isEqualTo("qrcode-123.png");
        assertThat(response.contentType()).isEqualTo("image/png");

        // E o metodo de upload deve ter sido chamado exatamente 1 vez com image/pgn
        verify(storage).uploadFile(any(byte[].class), any(String.class), eq("image/png"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o texto for nullo")
    void deveLancarExcecaoQuandoTextoForNulo(){
        //Texto nulo deve lançar exceção
        assertThatThrownBy(() -> qrCodeGeneratorService.generateAndUploadQrCode(null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nullo ou vazio");

    }

    @Test
    @DisplayName("Deve lançar exceção quando o texto for vazio")
    void deveLançarExcecaoQuandoTextoForVazion(){
        assertThatThrownBy(() -> qrCodeGeneratorService.generateAndUploadQrCode(""))
        .isInstanceOf(Exception.class).hasMessageContaining("nullo ou vazio");
    }
}
