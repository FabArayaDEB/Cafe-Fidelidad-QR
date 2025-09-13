package com.example.cafefidelidaqrdemo.models;

import java.util.Date;

/**
 * Modelo para representar el progreso general del cliente
 */
public class ProgresoGeneral {
    private String clienteId;
    private int puntosActuales;
    private int puntosCanjeados;
    private int puntosTotales;
    private int visitasActuales;
    private int visitasObjetivo;
    private int beneficiosDisponibles;
    private int beneficiosCanjeados;
    private String nivelActual;
    private String proximoNivel;
    private int puntosParaProximoNivel;
    private double progresoNivel;
    private Date ultimaVisita;
    private Date fechaRegistro;
    private boolean sincronizado;
    private Date fechaActualizacion;
    
    // Constructor vacío
    public ProgresoGeneral() {}
    
    // Constructor completo
    public ProgresoGeneral(String clienteId, int puntosActuales, int puntosCanjeados, 
                          int visitasActuales, int visitasObjetivo, int beneficiosDisponibles, 
                          int beneficiosCanjeados, String nivelActual, String proximoNivel, 
                          int puntosParaProximoNivel, Date ultimaVisita, Date fechaRegistro) {
        this.clienteId = clienteId;
        this.puntosActuales = puntosActuales;
        this.puntosCanjeados = puntosCanjeados;
        this.puntosTotales = puntosActuales + puntosCanjeados;
        this.visitasActuales = visitasActuales;
        this.visitasObjetivo = visitasObjetivo;
        this.beneficiosDisponibles = beneficiosDisponibles;
        this.beneficiosCanjeados = beneficiosCanjeados;
        this.nivelActual = nivelActual;
        this.proximoNivel = proximoNivel;
        this.puntosParaProximoNivel = puntosParaProximoNivel;
        this.progresoNivel = puntosParaProximoNivel > 0 ? (double) puntosActuales / puntosParaProximoNivel : 1.0;
        this.ultimaVisita = ultimaVisita;
        this.fechaRegistro = fechaRegistro;
        this.sincronizado = true;
        this.fechaActualizacion = new Date();
    }
    
    // Getters y Setters
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    
    public int getPuntosActuales() { return puntosActuales; }
    public void setPuntosActuales(int puntosActuales) { 
        this.puntosActuales = puntosActuales;
        this.puntosTotales = puntosActuales + puntosCanjeados;
        updateProgresoNivel();
    }
    
    public int getPuntosCanjeados() { return puntosCanjeados; }
    public void setPuntosCanjeados(int puntosCanjeados) { 
        this.puntosCanjeados = puntosCanjeados;
        this.puntosTotales = puntosActuales + puntosCanjeados;
    }
    
    public int getPuntosTotales() { return puntosTotales; }
    
    public int getVisitasActuales() { return visitasActuales; }
    public void setVisitasActuales(int visitasActuales) { this.visitasActuales = visitasActuales; }
    
    public int getVisitasObjetivo() { return visitasObjetivo; }
    public void setVisitasObjetivo(int visitasObjetivo) { this.visitasObjetivo = visitasObjetivo; }
    
    public int getBeneficiosDisponibles() { return beneficiosDisponibles; }
    public void setBeneficiosDisponibles(int beneficiosDisponibles) { this.beneficiosDisponibles = beneficiosDisponibles; }
    
    public int getBeneficiosCanjeados() { return beneficiosCanjeados; }
    public void setBeneficiosCanjeados(int beneficiosCanjeados) { this.beneficiosCanjeados = beneficiosCanjeados; }
    
    public String getNivelActual() { return nivelActual; }
    public void setNivelActual(String nivelActual) { this.nivelActual = nivelActual; }
    
    public String getProximoNivel() { return proximoNivel; }
    public void setProximoNivel(String proximoNivel) { this.proximoNivel = proximoNivel; }
    
    public int getPuntosParaProximoNivel() { return puntosParaProximoNivel; }
    public void setPuntosParaProximoNivel(int puntosParaProximoNivel) { 
        this.puntosParaProximoNivel = puntosParaProximoNivel;
        updateProgresoNivel();
    }
    
    public double getProgresoNivel() { return progresoNivel; }
    
    public Date getUltimaVisita() { return ultimaVisita; }
    public void setUltimaVisita(Date ultimaVisita) { this.ultimaVisita = ultimaVisita; }
    
    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    
    public Date getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Date fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    // Métodos de utilidad
    private void updateProgresoNivel() {
        this.progresoNivel = puntosParaProximoNivel > 0 ? (double) puntosActuales / puntosParaProximoNivel : 1.0;
    }
    
    public double getProgresoVisitas() {
        return visitasObjetivo > 0 ? (double) visitasActuales / visitasObjetivo : 0.0;
    }
    
    public boolean tieneProximoNivel() {
        return proximoNivel != null && !proximoNivel.isEmpty();
    }
    
    public boolean estaEnUltimoNivel() {
        return !tieneProximoNivel();
    }
    
    public int getPuntosRestantesParaProximoNivel() {
        return Math.max(0, puntosParaProximoNivel - puntosActuales);
    }
    
    public int getVisitasRestantesParaObjetivo() {
        return Math.max(0, visitasObjetivo - visitasActuales);
    }
    
    public int getPorcentajeProgreso() {
        if (puntosParaProximoNivel <= 0) {
            return 100;
        }
        return (int) ((puntosActuales * 100.0) / puntosParaProximoNivel);
    }
}