package com.frimesa.balanca.controller;

import com.frimesa.balanca.dto.TicketDTO;
import com.frimesa.balanca.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private GoogleSheetsService service;

    @GetMapping
    public List<TicketDTO> listarTodos() {
        return service.buscarDadosPlanilha();
    }
}
