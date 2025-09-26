package com.example.cafefidelidaqrdemo.database.models;

public class Canje {
    private int id;
    private int clienteId;
    private int beneficioId;
    private String fechaCanje;
    private int puntosUtilizados;
    private String estado;

    // Constructor vacío
    public Canje() {
    }

    // Constructor completo
    public Canje(int id, int clienteId, int beneficioId, String fechaCanje, int puntosUtilizados, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.beneficioId = beneficioId;
        this.fechaCanje = fechaCanje;
        this.puntosUtilizados = puntosUtilizados;
        this.estado = estado;
    }

    // Constructor sin ID (para inserción)
    public Canje(int clienteId, int beneficioId, String fechaCanje, int puntosUtilizados, String estado) {
        this.clienteId = clienteId;
        this.beneficioId = beneficioId;
        this.fechaCanje = fechaCanje;
        this.puntosUtilizados = puntosUtilizados;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getBeneficioId() {
        return beneficioId;
    }

    public void setBeneficioId(int beneficioId) {
        this.beneficioId = beneficioId;
    }

    public String getFechaCanje() {
        return fechaCanje;
    }

    public void setFechaCanje(String fechaCanje) {
        this.fechaCanje = fechaCanje;
    }

    public int getPuntosUtilizados() {
        return puntosUtilizados;
    }

    public void setPuntosUtilizados(int puntosUtilizados) {
        this.puntosUtilizados = puntosUtilizados;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Canje{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", beneficioId=" + beneficioId +
                ", fechaCanje='" + fechaCanje + '\'' +
                ", puntosUtilizados=" + puntosUtilizados +
                ", estado='" + estado + '\'' +
                '}';
    }
}