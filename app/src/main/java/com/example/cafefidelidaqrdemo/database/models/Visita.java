package com.example.cafefidelidaqrdemo.database.models;

public class Visita {
    private int id;
    private int clienteId;
    private int sucursalId;
    private String fechaVisita;
    private int puntosGanados;

    // Constructor vacío
    public Visita() {
    }

    // Constructor completo
    public Visita(int id, int clienteId, int sucursalId, String fechaVisita, int puntosGanados) {
        this.id = id;
        this.clienteId = clienteId;
        this.sucursalId = sucursalId;
        this.fechaVisita = fechaVisita;
        this.puntosGanados = puntosGanados;
    }

    // Constructor sin ID (para inserción)
    public Visita(int clienteId, int sucursalId, String fechaVisita, int puntosGanados) {
        this.clienteId = clienteId;
        this.sucursalId = sucursalId;
        this.fechaVisita = fechaVisita;
        this.puntosGanados = puntosGanados;
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

    public int getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(int sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(String fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public int getPuntosGanados() {
        return puntosGanados;
    }

    public void setPuntosGanados(int puntosGanados) {
        this.puntosGanados = puntosGanados;
    }

    @Override
    public String toString() {
        return "Visita{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", sucursalId=" + sucursalId +
                ", fechaVisita='" + fechaVisita + '\'' +
                ", puntosGanados=" + puntosGanados +
                '}';
    }
}