package com.frimesa.balanca.service;

import com.frimesa.balanca.dto.TicketDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GoogleSheetsService {

    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vR7xkoiMc8GHf7ExRRvhBtQqkvEYuFbspMIxuu-1eHZ-LJd0IwfpBBxWAKXHVnGAg/pub?gid=2099001465&single=true&output=csv";

    private static final Map<String, Double> TOLERANCIAS = Map.of(
        "TOLERÂNCIA UTILITARIO", 1.5,
        "TOLERÂNCIA VAN", 3.0,
        "TOLERÂNCIA VUC", 5.0,
        "TOLERÂNCIA 34", 8.0,
        "TOLERÂNCIA TOCO", 12.0,
        "TOLERÂNCIA TRUCK", 24.0,
        "TOLERÂNCIA BITRUCK", 32.0,
        "TOLERÂNCIA CARRETA", 56.0
    );

    public List<TicketDTO> buscarDadosPlanilha() {
        List<TicketDTO> lista = new ArrayList<>();
        
        try {
            URL url = new URL(CSV_URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;

                while ((linha = reader.readLine()) != null) {
                    if (linha.trim().isEmpty()) continue;
                    
                    // Pula cabeçalho inicial
                    if (primeiraLinha) { 
                        primeiraLinha = false; 
                        continue; 
                    }

                    // Trata separador de vírgula ou ponto e vírgula
                    String separador = linha.contains(";") ? ";" : ",";
                    String[] col = linha.split(separador, -1);

                    // Garante que exista ao menos 2 colunas e que a primeira não seja vazia
                    if (col.length < 2) continue;
                    String idExtraido = limparTexto(col[0]);
                    if (idExtraido.isEmpty() || idExtraido.equalsIgnoreCase("ID")) continue;

                    TicketDTO ticket = new TicketDTO();
                    ticket.setId(idExtraido);
                    ticket.setPlaca(col.length > 1 ? limparTexto(col[1]) : "");

                    ticket.setDataEntrada(col.length > 2 ? converterData(col[2]) : LocalDateTime.now());
                    ticket.setDataSaida(col.length > 3 ? converterData(col[3]) : LocalDateTime.now());

                    ticket.setLacres(col.length > 4 ? limparTexto(col[4]) : "");
                    ticket.setQuantPallets(col.length > 5 ? converterInteiro(col[5]) : 0);
                    ticket.setPesoPallets(col.length > 6 ? converterDouble(col[6]) : 0.0);
                    ticket.setPesoCheio(col.length > 7 ? converterDouble(col[7]) : 0.0);
                    ticket.setPesoVazio(col.length > 8 ? converterDouble(col[8]) : 0.0);
                    ticket.setPesoNf(col.length > 9 ? converterDouble(col[9]) : 0.0);

                    double pesoLiquido = ticket.getPesoCheio() - ticket.getPesoVazio();
                    double divergencia = pesoLiquido - ticket.getPesoNf();

                    ticket.setPesoLiquido(pesoLiquido);
                    ticket.setDivergencia(divergencia);

                    String tolTipo = (col.length > 10 && !col[10].trim().isEmpty()) ? limparTexto(col[10]) : "TOLERÂNCIA TRUCK";
                    ticket.setToleranciaTipo(tolTipo);

                    double limiteKg = TOLERANCIAS.getOrDefault(tolTipo.toUpperCase(), 24.0);
                    ticket.setLimiteToleranciaKg(limiteKg);

                    if (Math.abs(divergencia) <= limiteKg) {
                        ticket.setResultado("DENTRO DA TOLERÂNCIA");
                        ticket.setStatus("OK");
                    } else if (divergencia < 0) {
                        ticket.setResultado("FORA DA TOLERÂNCIA");
                        ticket.setStatus("FALTA");
                    } else {
                        ticket.setResultado("FORA DA TOLERÂNCIA");
                        ticket.setStatus("SOBRA");
                    }

                    lista.add(ticket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private String limparTexto(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "").replaceAll("[\\r\\n]", "").trim();
    }

    private Double converterDouble(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) return 0.0;
            String limpo = valor.replace("\"", "").replace(".", "").replace(",", ".").trim();
            return Double.parseDouble(limpo);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Integer converterInteiro(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) return 0;
            String limpo = valor.replace("\"", "").replaceAll("[^0-9]", "").trim();
            return Integer.parseInt(limpo);
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalDateTime converterData(String valor) {
        try {
            String valLimpo = limparTexto(valor);
            if (valLimpo.isEmpty()) return LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return LocalDateTime.parse(valLimpo, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
