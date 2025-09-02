package com.example.cafefidelidaqrdemo;

public class Transaccion {
    private String id;
    private String descripcion;
    private String fechaHora;
    private double monto;
    private int puntosGanados;
    private String tipo; // "compra" o "qr"
    private String userId;
    private long timestamp;

    public Transaccion() {
        // Constructor vacío requerido para Firebase
    }

    public Transaccion(String descripcion, String fechaHora, double monto, int puntosGanados, String tipo, String userId) {
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.monto = monto;
        this.puntosGanados = puntosGanados;
        this.tipo = tipo;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public double getMonto() {
        return monto;
    }

    public int getPuntosGanados() {
        return puntosGanados;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public void setPuntosGanados(int puntosGanados) {
        this.puntosGanados = puntosGanados;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}