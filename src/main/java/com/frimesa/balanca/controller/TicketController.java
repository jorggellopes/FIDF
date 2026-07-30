package com.frimesa.balanca.controller;

import com.frimesa.balanca.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Libera acesso para o site
public class TicketController {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    // URL do seu Google Apps Script WebApp
    private static final String GOOGLE_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxD39BgHL_IxRXP_e2n0zqx-y3Ub3ynNUXMMMiuZRElXNrP7OAFjssazRgmvlxnh_VZSA/exec";

    // Rota de Leitura (Já existente)
    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(googleSheetsService.buscarDadosPlanilha());
    }

    // ROTA PONTE DE GRAVAÇÃO (FAZ A PONTE SITE -> JAVA -> GOOGLE)
    @PostMapping("/gravar")
    public ResponseEntity<String> gravarTicket(@RequestBody Map<String, Object> payload) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // O Java envia direto para o Google sem restrição de CORS
            ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_SCRIPT_URL, request, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"status\":\"ERROR\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
