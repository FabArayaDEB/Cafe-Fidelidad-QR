package com.example.cafefidelidaqrdemo.database.models;

import androidx.room.ColumnInfo;

/**
 * Clase para métricas de visitas obtenidas de consultas de reportes
 */
public class MetricasVisitas {
    @ColumnInfo(name = "totalVisitas")
    public int totalVisitas;
    
    @ColumnInfo(name = "nuevosClientes")
    public int nuevosClientes;
    
    @ColumnInfo(name = "clientesRecurrentes")
    public int clientesRecurrentes;
    
    @ColumnInfo(name = "promedioVisitasPorCliente")
    public double promedioVisitasPorCliente;
    
    public MetricasVisitas() {
        // Constructor vacío requerido por Room
    }
    
    public MetricasVisitas(int totalVisitas, int nuevosClientes, int clientesRecurrentes, double promedioVisitasPorCliente) {
        this.totalVisitas = totalVisitas;
        this.nuevosClientes = nuevosClientes;
        this.clientesRecurrentes = clientesRecurrentes;
        this.promedioVisitasPorCliente = promedioVisitasPorCliente;
    }
    
    // Getters y setters
    public int getTotalVisitas() { return totalVisitas; }
    public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
    
    public int getNuevosClientes() { return nuevosClientes; }
    public void setNuevosClientes(int nuevosClientes) { this.nuevosClientes = nuevosClientes; }
    
    public int getClientesRecurrentes() { return clientesRecurrentes; }
    public void setClientesRecurrentes(int clientesRecurrentes) { this.clientesRecurrentes = clientesRecurrentes; }
    
    public double getPromedioVisitasPorCliente() { return promedioVisitasPorCliente; }
    public void setPromedioVisitasPorCliente(double promedioVisitasPorCliente) { this.promedioVisitasPorCliente = promedioVisitasPorCliente; }
}