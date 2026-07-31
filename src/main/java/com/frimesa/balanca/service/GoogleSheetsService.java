package com.frimesa.balanca.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frimesa.balanca.dto.TicketDTO;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GoogleSheetsService {

    // IDENTIFICADORES DA API DO APPSHEET
    private static final String APP_ID = "b5fbbbbc-a969-499f-858d-a50acab02d5c";
    private static final String ACCESS_KEY = "V2-drcFW-JI6tk-93uYP-MFvMx-XwQxC-eMjCI-z6tM5-W21kN";
    private static final String TABLE_NAME = "Página1"; 

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
            URL url = new URL("https://api.appsheet.com/api/v2/apps/" + APP_ID + "/tables/" + TABLE_NAME + "/Action");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("ApplicationAccessKey", ACCESS_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Payload para consultar os registros via API oficial do AppSheet
            String jsonPayload = "{\"Action\": \"Find\", \"Properties\": {}, \"Rows\": []}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(conn.getInputStream());

                if (rootNode.isArray()) {
                    for (JsonNode row : rootNode) {
                        String idVal = getText(row, "ID");
                        if (idVal.isEmpty()) continue;

                        TicketDTO ticket = new TicketDTO();
                        ticket.setId(idVal);

                        String placa = getText(row, "PLACA").toUpperCase();
                        ticket.setPlaca(placa);

                        String veiculo = getText(row, "Veiculo / Modelo");
                        ticket.setVeiculoModelo(veiculo);

                        ticket.setMotorista(formatarNomeProprio(getText(row, "MOTORISTA")));
                        ticket.setDoca(getText(row, "Nº DA DOCA").toUpperCase());
                        ticket.setMovimento(getText(row, "MOVIMENTO").toUpperCase());

                        String tipoCarga = getText(row, "TIPO DE CARGA").toUpperCase();
                        ticket.setTipoCarga(tipoCarga);

                        ticket.setNumCarga(getText(row, "Nº CARGA"));
                        ticket.setNf(getText(row, "NF-e"));

                        String dtEntrada = getText(row, "Data de Entrada");
                        String dtSaida = getText(row, "Data de saída");
                        ticket.setDataEntradaTexto(dtEntrada);
                        ticket.setDataSaidaTexto(dtSaida);

                        ticket.setLacres(getText(row, "Nº LACRE"));
                        ticket.setQuantPallets(getInt(row, "Qt Pallets"));
                        ticket.setPesoPallets(getDouble(row, "PESO PALLET"));

                        double pesoCheio = getDouble(row, "PESO CHEIO ()");
                        double pesoVazio = getDouble(row, "PESO VAZIO ()");
                        double pesoNf = getDouble(row, "Peso NF ()");

                        ticket.setPesoCheio(pesoCheio);
                        ticket.setPesoVazio(pesoVazio);
                        ticket.setPesoNf(pesoNf);

                        double pesoLiquido = (pesoCheio > 0 && pesoVazio > 0) ? (pesoCheio - pesoVazio) : getDouble(row, "Peso Liquido ()");
                        double divergencia = (pesoLiquido > 0) ? (pesoLiquido - pesoNf) : getDouble(row, "Divergência ()");

                        ticket.setPesoLiquido(pesoLiquido);
                        ticket.setDivergencia(divergencia);

                        ticket.setConferente(formatarNomeProprio(getText(row, "Conferente")));

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
            } else {
                System.err.println("Erro na chamada da API do AppSheet: HTTP " + conn.getResponseCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText().trim() : "";
    }

    private double getDouble(JsonNode node, String field) {
        try {
            if (node.has(field) && !node.get(field).isNull()) {
                String val = node.get(field).asText().replace(".", "").replace(",", ".").trim();
                return Double.parseDouble(val);
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    private int getInt(JsonNode node, String field) {
        try {
            if (node.has(field) && !node.get(field).isNull()) {
                return Integer.parseInt(node.get(field).asText().replaceAll("[^0-9]", "").trim());
            }
        } catch (Exception ignored) {}
        return 0;
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
