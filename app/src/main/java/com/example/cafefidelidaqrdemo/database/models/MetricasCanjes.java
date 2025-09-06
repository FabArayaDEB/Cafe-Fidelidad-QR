package com.example.cafefidelidaqrdemo.database.models;

import androidx.room.ColumnInfo;

/**
 * Clase para métricas de canjes obtenidas de consultas de reportes
 */
public class MetricasCanjes {
    @ColumnInfo(name = "totalCanjes")
    public int totalCanjes;
    
    @ColumnInfo(name = "puntosCanjeados")
    public int puntosCanjeados;
    
    @ColumnInfo(name = "beneficiosUtilizados")
    public int beneficiosUtilizados;
    
    @ColumnInfo(name = "promedioCanjesPorCliente")
    public double promedioCanjesPorCliente;
    
    public MetricasCanjes() {
        // Constructor vacío requerido por Room
    }
    
    public MetricasCanjes(int totalCanjes, int puntosCanjeados, int beneficiosUtilizados, double promedioCanjesPorCliente) {
        this.totalCanjes = totalCanjes;
        this.puntosCanjeados = puntosCanjeados;
        this.beneficiosUtilizados = beneficiosUtilizados;
        this.promedioCanjesPorCliente = promedioCanjesPorCliente;
    }
    
    // Getters y setters
    public int getTotalCanjes() { return totalCanjes; }
    public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
    
    public int getPuntosCanjeados() { return puntosCanjeados; }
    public void setPuntosCanjeados(int puntosCanjeados) { this.puntosCanjeados = puntosCanjeados; }
    
    public int getBeneficiosUtilizados() { return beneficiosUtilizados; }
    public void setBeneficiosUtilizados(int beneficiosUtilizados) { this.beneficiosUtilizados = beneficiosUtilizados; }
    
    public double getPromedioCanjesPorCliente() { return promedioCanjesPorCliente; }
    public void setPromedioCanjesPorCliente(double promedioCanjesPorCliente) { this.promedioCanjesPorCliente = promedioCanjesPorCliente; }
}