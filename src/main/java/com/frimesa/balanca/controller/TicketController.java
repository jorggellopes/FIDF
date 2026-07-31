package com.frimesa.balanca.controller;

import com.frimesa.balanca.dto.TicketDTO;
import com.frimesa.balanca.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Permite chamadas do frontend Vercel sem erro de CORS
public class TicketController {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    @GetMapping
    public List<TicketDTO> listarTodos() {
        return googleSheetsService.buscarDadosPlanilha();
    }
}
