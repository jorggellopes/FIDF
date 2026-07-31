package com.frimesa.balanca.service;

import com.frimesa.balanca.dto.TicketDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GoogleSheetsService {

    // URL DO PROXY APPS SCRIPT NA ORGANIZAÇÃO FRIMESA
    private static final String CSV_URL = "https://script.google.com/a/macros/frimesa.com.br/s/AKfycbw9Eaqn_HdhSsl9Ya64hum7kCi3ZRLyjQN4FGIskTLLDklogOAVOi6rwH9bL1ifgg/exec";

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
            URL url = new URL(CSV_URL + "?_t=" + System.currentTimeMillis());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;

                while ((linha = reader.readLine()) != null) {
                    if (linha.trim().isEmpty()) continue;
                    
                    if (primeiraLinha) { 
                        primeiraLinha = false; 
                        continue; 
                    }

                    String[] col = linha.split("(?!\"[^\"]*),(?![^\"]*\")|;", -1);
                    if (col.length < 1) continue;

                    String idVal = limparTexto(col[0]);
                    if (idVal.isEmpty() || idVal.equalsIgnoreCase("ID")) continue;

                    TicketDTO ticket = new TicketDTO();
                    ticket.setId(idVal);
                    
                    String placa = col.length > 1 ? limparTexto(col[1]).toUpperCase() : "";
                    ticket.setPlaca(placa);

                    String veiculo = col.length > 2 ? limparTexto(col[2]) : "";
                    ticket.setVeiculoModelo(veiculo);

                    ticket.setMotorista(col.length > 3 ? formatarNomeProprio(col[3]) : "");
                    ticket.setDoca(col.length > 4 ? limparTexto(col[4]).toUpperCase() : "");
                    ticket.setMovimento(col.length > 5 ? limparTexto(col[5]).toUpperCase() : "");
                    
                    String tipoCarga = col.length > 6 ? limparTexto(col[6]).toUpperCase() : "";
                    ticket.setTipoCarga(tipoCarga);

                    ticket.setNumCarga(col.length > 7 ? limparTexto(col[7]) : "");
                    ticket.setNf(col.length > 8 ? limparTexto(col[8]) : "");

                    String dtEntrada = col.length > 9 ? limparTexto(col[9]) : "";
                    String dtSaida = col.length > 10 ? limparTexto(col[10]) : "";
                    ticket.setDataEntradaTexto(dtEntrada);
                    ticket.setDataSaidaTexto(dtSaida);

                    ticket.setLacres(col.length > 11 ? limparTexto(col[11]) : "");
                    ticket.setQuantPallets(col.length > 12 ? converterInteiro(col[12]) : 0);
                    ticket.setPesoPallets(col.length > 13 ? converterDouble(col[13]) : 0.0);

                    double pesoCheio = col.length > 14 ? converterDouble(col[14]) : 0.0;
                    double pesoVazio = col.length > 15 ? converterDouble(col[15]) : 0.0;
                    double pesoNf = col.length > 17 ? converterDouble(col[17]) : 0.0;

                    ticket.setPesoCheio(pesoCheio);
                    ticket.setPesoVazio(pesoVazio);
                    ticket.setPesoNf(pesoNf);

                    double pesoLiquido = (pesoCheio > 0 && pesoVazio > 0) ? (pesoCheio - pesoVazio) : (col.length > 16 ? converterDouble(col[16]) : 0.0);
                    double divergencia = (pesoLiquido > 0) ? (pesoLiquido - pesoNf) : (col.length > 18 ? converterDouble(col[18]) : 0.0);

                    ticket.setPesoLiquido(pesoLiquido);
                    ticket.setDivergencia(divergencia);

                    ticket.setConferente(col.length > 19 ? formatarNomeProprio(col[19]) : "");

                    String tolTipo = identificarToleranciaPorVeiculo(veiculo);
                    ticket.setToleranciaTipo(tolTipo);

                    double limiteKg = TOLERANCIAS.getOrDefault(tolTipo.toUpperCase(), 24.0);
                    ticket.setLimiteToleranciaKg(limiteKg);

                    if (placa.isEmpty() || placa.equals("---") || dtEntrada.isEmpty() || dtEntrada.equals("---")) {
                        ticket.setStatus("S/D");
                        ticket.setResultado("SEM DADOS REGISTRADOS");
                    } else if (tipoCarga.contains("VAZIO") || tipoCarga.contains("PATIO")) {
                        ticket.setStatus("VAZIO");
                        ticket.setResultado("VEÍCULO VAZIO / PÁTIO");
                    } else if (Math.abs(divergencia) <= limiteKg) {
                        ticket.setStatus("OK");
                        ticket.setResultado("DENTRO DA TOLERÂNCIA");
                    } else if (divergencia < 0) {
                        ticket.setStatus("FALTA");
                        ticket.setResultado("FORA DA TOLERÂNCIA");
                    } else {
                        ticket.setStatus("SOBRA");
                        ticket.setResultado("FORA DA TOLERÂNCIA");
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

    private String formatarNomeProprio(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String limpo = str.trim();
        if (limpo.length() < 2) return limpo;

        String[] palavras = limpo.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String p : palavras) {
            if (List.of("de", "da", "do", "dos", "das", "e").contains(p)) {
                sb.append(p).append(" ");
            } else if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String identificarToleranciaPorVeiculo(String veiculo) {
        if (veiculo == null || veiculo.isEmpty()) return "TOLERÂNCIA TRUCK";
        String v = veiculo.toUpperCase();
        if (v.contains("VAN")) return "TOLERÂNCIA VAN";
        if (v.contains("VUC")) return "TOLERÂNCIA VUC";
        if (v.contains("3/4") || v.contains("34") || v.contains("3QT")) return "TOLERÂNCIA 34";
        if (v.contains("TOCO")) return "TOLERÂNCIA TOCO";
        if (v.contains("BITRUCK")) return "TOLERÂNCIA BITRUCK";
        if (v.contains("TRUCK")) return "TOLERÂNCIA TRUCK";
        if (v.contains("CARRETA") || v.contains("BITREM")) return "TOLERÂNCIA CARRETA";
        if (v.contains("UTILITARIO") || v.contains("PARTICULAR")) return "TOLERÂNCIA UTILITARIO";
        return "TOLERÂNCIA TRUCK";
    }
}
