package com.example.cafefidelidaqrdemo.models;

import java.util.Date;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;

/**
 * Modelo para representar un próximo beneficio a desbloquear
 */
public class ProximoBeneficio {
    private String beneficioId;
    private String nombre;
    private String descripcion;
    private int puntosRequeridos;
    private int puntosActuales;
    private int puntosFaltantes;
    private double valor;
    private String categoria;
    private Date fechaDisponible;
    private String imagenUrl;
    private boolean esRecomendado;
    private String razonRecomendacion;
    private int prioridad;
    private int visitasRequeridas;
    private int visitasFaltantes;
    private BeneficioEntity beneficio;
    private double progreso;
    
    // Constructor vacío
    public ProximoBeneficio() {}
    
    // Constructor completo
    public ProximoBeneficio(String beneficioId, String nombre, String descripcion, 
                           int puntosRequeridos, int puntosActuales, double valor, 
                           String categoria, Date fechaDisponible, String imagenUrl, 
                           boolean esRecomendado, String razonRecomendacion, int prioridad) {
        this.beneficioId = beneficioId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosRequeridos = puntosRequeridos;
        this.puntosActuales = puntosActuales;
        this.puntosFaltantes = Math.max(0, puntosRequeridos - puntosActuales);
        this.valor = valor;
        this.categoria = categoria;
        this.fechaDisponible = fechaDisponible;
        this.imagenUrl = imagenUrl;
        this.esRecomendado = esRecomendado;
        this.razonRecomendacion = razonRecomendacion;
        this.prioridad = prioridad;
    }
    
    // Getters y Setters
    public String getBeneficioId() { return beneficioId; }
    public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public int getPuntosRequeridos() { return puntosRequeridos; }
    public void setPuntosRequeridos(int puntosRequeridos) { 
        this.puntosRequeridos = puntosRequeridos;
        this.puntosFaltantes = Math.max(0, puntosRequeridos - puntosActuales);
    }
    
    public int getPuntosActuales() { return puntosActuales; }
    public void setPuntosActuales(int puntosActuales) { 
        this.puntosActuales = puntosActuales;
        this.puntosFaltantes = Math.max(0, puntosRequeridos - puntosActuales);
    }
    
    public int getPuntosFaltantes() { return puntosFaltantes; }
    
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public Date getFechaDisponible() { return fechaDisponible; }
    public void setFechaDisponible(Date fechaDisponible) { this.fechaDisponible = fechaDisponible; }
    
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    public boolean isEsRecomendado() { return esRecomendado; }
    public void setEsRecomendado(boolean esRecomendado) { this.esRecomendado = esRecomendado; }
    
    public String getRazonRecomendacion() { return razonRecomendacion; }
    public void setRazonRecomendacion(String razonRecomendacion) { this.razonRecomendacion = razonRecomendacion; }
    
    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }
    
    // Métodos de utilidad
    public double getProgresoPercentage() {
        if (puntosRequeridos == 0) return 100.0;
        return Math.min(100.0, (double) puntosActuales / puntosRequeridos * 100.0);
    }
    
    public boolean estaCercaDeDesbloquear() {
        return getProgresoPercentage() >= 80.0;
    }
    
    public boolean puedeDesbloquearse() {
        return puntosActuales >= puntosRequeridos;
    }
    
    public String getDescripcionProgreso() {
        return String.format("%d de %d puntos (%d faltantes)", 
                           puntosActuales, puntosRequeridos, puntosFaltantes);
    }
    
    public String getDescripcionCompleta() {
        return String.format("%s - %s (Valor: $%.2f)", 
                           nombre, descripcion, valor);
    }
    
    // Métodos adicionales requeridos por el adapter
    public BeneficioEntity getBeneficio() {
        if (beneficio == null) {
            // Crear un BeneficioEntity temporal con los datos disponibles
            beneficio = new BeneficioEntity();
            beneficio.setId_beneficio(beneficioId);
            beneficio.setNombre(nombre);
            // BeneficioEntity no tiene setDescripcion, setValor, setCategoria, setImagenUrl, setActivo
            // Estos métodos no están disponibles en la entidad de database
        }
        return beneficio;
    }
    
    public void setBeneficio(BeneficioEntity beneficio) {
        this.beneficio = beneficio;
        if (beneficio != null) {
            this.beneficioId = beneficio.getId_beneficio();
            this.nombre = beneficio.getNombre();
            // Los métodos getDescripcion, getValor, getCategoria, getImagenUrl no están disponibles
            // en BeneficioEntity de database.entities
        }
    }
    
    public double getProgreso() {
        return progreso;
    }
    
    public void setProgreso(double progreso) {
        this.progreso = progreso;
    }
    
    public int getVisitasRequeridas() {
        return visitasRequeridas;
    }
    
    public void setVisitasRequeridas(int visitasRequeridas) {
        this.visitasRequeridas = visitasRequeridas;
        this.visitasFaltantes = Math.max(0, visitasRequeridas - (visitasRequeridas - visitasFaltantes));
    }
    
    public int getVisitasFaltantes() {
        return visitasFaltantes;
    }
    
    public void setVisitasFaltantes(int visitasFaltantes) {
        this.visitasFaltantes = visitasFaltantes;
    }
    
    @Override
    public String toString() {
        return "ProximoBeneficio{" +
                "beneficioId='" + beneficioId + '\'' +
                ", nombre='" + nombre + '\'' +
                ", puntosRequeridos=" + puntosRequeridos +
                ", puntosActuales=" + puntosActuales +
                ", puntosFaltantes=" + puntosFaltantes +
                ", valor=" + valor +
                ", categoria='" + categoria + '\'' +
                ", esRecomendado=" + esRecomendado +
                '}';
    }
}