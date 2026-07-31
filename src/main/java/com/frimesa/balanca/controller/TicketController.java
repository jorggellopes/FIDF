package com.frimesa.balanca.controller;

import com.frimesa.balanca.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    // URL DO NOVO WEB APP DO GOOGLE APPS SCRIPT
    private static final String GOOGLE_SCRIPT_URL = "https://script.google.com/a/macros/frimesa.com.br/s/AKfycbw9Eaqn_HdhSsl9Ya64hum7kCi3ZRLyjQN4FGIskTLLDklogOAVOi6rwH9bL1ifgg/exec";

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(googleSheetsService.buscarDadosPlanilha());
    }

    @PostMapping("/gravar")
    public ResponseEntity<String> gravarTicket(@RequestBody Map<String, Object> payload) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_SCRIPT_URL, request, String.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"sucesso\": false, \"erro\": \"" + e.getMessage() + "\"}");
        }
    }
}
