package com.example.cafefidelidaqrdemo.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

/**
 * Clase para representar los clientes top obtenidos de consultas de reportes
 */
public class TopCliente {
    @ColumnInfo(name = "clienteId")
    public String clienteId;
    
    @ColumnInfo(name = "nombre")
    public String nombre;
    
    @ColumnInfo(name = "totalVisitas")
    public int totalVisitas;
    
    @ColumnInfo(name = "totalCanjes")
    public int totalCanjes;
    
    @ColumnInfo(name = "totalPuntos")
    public int totalPuntos;
    
    @ColumnInfo(name = "valorTotalCanjes")
    public double valorTotalCanjes;
    
    @ColumnInfo(name = "sucursalFavorita")
    public String sucursalFavorita;
    
    public TopCliente() {
        // Constructor vacío requerido por Room
    }
    
    @Ignore
    public TopCliente(String clienteId, String nombre, int totalVisitas, int totalCanjes, int totalPuntos) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.totalVisitas = totalVisitas;
        this.totalCanjes = totalCanjes;
        this.totalPuntos = totalPuntos;
        this.valorTotalCanjes = 0.0;
        this.sucursalFavorita = "";
    }
    
    // Getters y setters
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public int getTotalVisitas() { return totalVisitas; }
    public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
    
    public int getTotalCanjes() { return totalCanjes; }
    public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
    
    public int getTotalPuntos() { return totalPuntos; }
    public void setTotalPuntos(int totalPuntos) { this.totalPuntos = totalPuntos; }
    
    public double getValorTotalCanjes() { return valorTotalCanjes; }
    public void setValorTotalCanjes(double valorTotalCanjes) { this.valorTotalCanjes = valorTotalCanjes; }
    
    public String getSucursalFavorita() { return sucursalFavorita; }
    public void setSucursalFavorita(String sucursalFavorita) { this.sucursalFavorita = sucursalFavorita; }
}