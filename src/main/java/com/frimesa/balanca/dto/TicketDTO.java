package com.frimesa.balanca.dto;

public class TicketDTO {
    private String id;
    private String placa;
    private String dataEntradaTexto;
    private String dataSaidaTexto;
    private String lacres;
    private Integer quantPallets;
    private Double pesoPallets;
    private Double pesoCheio;
    private Double pesoVazio;
    private Double pesoLiquido;
    private Double pesoNf;
    private Double divergencia;
    private String toleranciaTipo;
    private Double limiteToleranciaKg;
    private String resultado;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getDataEntradaTexto() { return dataEntradaTexto; }
    public void setDataEntradaTexto(String dataEntradaTexto) { this.dataEntradaTexto = dataEntradaTexto; }
    public String getDataSaidaTexto() { return dataSaidaTexto; }
    public void setDataSaidaTexto(String dataSaidaTexto) { this.dataSaidaTexto = dataSaidaTexto; }
    public String getLacres() { return lacres; }
    public void setLacres(String lacres) { this.lacres = lacres; }
    public Integer getQuantPallets() { return quantPallets; }
    public void setQuantPallets(Integer quantPallets) { this.quantPallets = quantPallets; }
    public Double getPesoPallets() { return pesoPallets; }
    public void setPesoPallets(Double pesoPallets) { this.pesoPallets = pesoPallets; }
    public Double getPesoCheio() { return pesoCheio; }
    public void setPesoCheio(Double pesoCheio) { this.pesoCheio = pesoCheio; }
    public Double getPesoVazio() { return pesoVazio; }
    public void setPesoVazio(Double pesoVazio) { this.pesoVazio = pesoVazio; }
    public Double getPesoLiquido() { return pesoLiquido; }
    public void setPesoLiquido(Double pesoLiquido) { this.pesoLiquido = pesoLiquido; }
    public Double getPesoNf() { return pesoNf; }
    public void setPesoNf(Double pesoNf) { this.pesoNf = pesoNf; }
    public Double getDivergencia() { return divergencia; }
    public void setDivergencia(Double divergencia) { this.divergencia = divergencia; }
    public String getToleranciaTipo() { return toleranciaTipo; }
    public void setToleranciaTipo(String toleranciaTipo) { this.toleranciaTipo = toleranciaTipo; }
    public Double getLimiteToleranciaKg() { return limiteToleranciaKg; }
    public void setLimiteToleranciaKg(Double limiteToleranciaKg) { this.limiteToleranciaKg = limiteToleranciaKg; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
