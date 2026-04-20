package com.GeradorQrCode.qrcode.controller;

import com.GeradorQrCode.qrcode.dto.QrCodeGenerateResponse;
import com.GeradorQrCode.qrcode.service.QrCodeGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QrCodeController.class)
@Import(GlobalExceptionHandler.class)
class QrCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QrCodeGeneratorService qrCodeGeneratorService;

    @Test
    @DisplayName("Deve retornar 200 quando a requisição for válida")
    void deveRetornar200QuandoRequisicaoForValida() throws Exception {
        when(qrCodeGeneratorService.generateAndUploadQrCode(anyString()))
                .thenReturn(new QrCodeGenerateResponse(
                        "https://meu-bucket.s3.amazonaws.com/qrcode.png?signature=abc",
                        900L,
                        "qrcode.png",
                        "image/png"
                ));

        mockMvc.perform(post("/qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "https://github.com/meu-usuario"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("https://meu-bucket.s3.amazonaws.com/qrcode.png?signature=abc"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900L))
                .andExpect(jsonPath("$.fileName").value("qrcode.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o campo text estiver vazio")
    void deveRetornar400QuandoTextEstiverVazio() throws Exception {
        mockMvc.perform(post("/qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.text").value("O texto é obrigatorio"));
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorrer erro inesperado")
    void deveRetornar500QuandoOcorrerErroInesperado() throws Exception {
        when(qrCodeGeneratorService.generateAndUploadQrCode(anyString()))
                .thenThrow(new RuntimeException("falha inesperada"));

        mockMvc.perform(post("/qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "conteudo valido"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o service lançar IllegalArgumentException")
    void deveRetornar400QuandoServiceLancarIllegalArgumentException() throws Exception {
        when(qrCodeGeneratorService.generateAndUploadQrCode(anyString()))
                .thenThrow(new IllegalArgumentException("Texto inválido para gerar QR Code"));

        mockMvc.perform(post("/qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "text": "abc"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Texto inválido para gerar QR Code"));
    }
}
