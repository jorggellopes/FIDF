package com.frimesa.balanca.dto;

public class TicketDTO {

    private String id;
    private String placa;
    private String veiculoModelo;
    private String motorista;
    private String doca;
    private String movimento;
    private String tipoCarga;
    private String numCarga;
    private String nf;
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
    private String conferente;
    private String toleranciaTipo;
    private Double limiteToleranciaKg;
    private String resultado;
    private String status;

    public TicketDTO() {}

    // GETTERS E SETTERS
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getVeiculoModelo() { return veiculoModelo; }
    public void setVeiculoModelo(String veiculoModelo) { this.veiculoModelo = veiculoModelo; }

    public String getMotorista() { return motorista; }
    public void setMotorista(String motorista) { this.motorista = motorista; }

    public String getDoca() { return doca; }
    public void setDoca(String doca) { this.doca = doca; }

    public String getMovimento() { return movimento; }
    public void setMovimento(String movimento) { this.movimento = movimento; }

    public String getTipoCarga() { return tipoCarga; }
    public void setTipoCarga(String tipoCarga) { this.tipoCarga = tipoCarga; }

    public String getNumCarga() { return numCarga; }
    public void setNumCarga(String numCarga) { this.numCarga = numCarga; }

    public String getNf() { return nf; }
    public void setNf(String nf) { this.nf = nf; }

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

    public String getConferente() { return conferente; }
    public void setConferente(String conferente) { this.conferente = conferente; }

    public String getToleranciaTipo() { return toleranciaTipo; }
    public void setToleranciaTipo(String toleranciaTipo) { this.toleranciaTipo = toleranciaTipo; }

    public Double getLimiteToleranciaKg() { return limiteToleranciaKg; }
    public void setLimiteToleranciaKg(Double limiteToleranciaKg) { this.limiteToleranciaKg = limiteToleranciaKg; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
