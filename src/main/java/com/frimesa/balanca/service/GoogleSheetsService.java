package com.frimesa.balanca.service;

import com.frimesa.balanca.dto.TicketDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String linha;
                boolean primeiraLinha = true;

                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }

                    String[] col = linha.split(",", -1);
                    if (col.length < 10 || col[0].trim().isEmpty()) continue;

                    TicketDTO ticket = new TicketDTO();
                    ticket.setId(col[0].trim());
                    ticket.setPlaca(col[1].trim());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    ticket.setDataEntrada(LocalDateTime.parse(col[2].trim(), formatter));
                    ticket.setDataSaida(LocalDateTime.parse(col[3].trim(), formatter));

                    ticket.setLacres(col[4].trim());
                    ticket.setQuantPallets(Integer.parseInt(col[5].trim()));
                    ticket.setPesoPallets(Double.parseDouble(col[6].trim().replace(",", ".")));
                    ticket.setPesoCheio(Double.parseDouble(col[7].trim().replace(",", ".")));
                    ticket.setPesoVazio(Double.parseDouble(col[8].trim().replace(",", ".")));
                    ticket.setPesoNf(Double.parseDouble(col[9].trim().replace(",", ".")));

                    double pesoLiquido = ticket.getPesoCheio() - ticket.getPesoVazio();
                    double divergencia = pesoLiquido - ticket.getPesoNf();

                    ticket.setPesoLiquido(pesoLiquido);
                    ticket.setDivergencia(divergencia);

                    String tolTipo = (col.length > 10 && !col[10].trim().isEmpty()) ? col[10].trim() : "TOLERÂNCIA TRUCK";
                    ticket.setToleranciaTipo(tolTipo);

                    double limiteKg = TOLERANCIAS.getOrDefault(tolTipo, 24.0);
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
}
