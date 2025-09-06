package com.example.cafefidelidaqrdemo.models;

/**
 * Modelo para representar actividades recientes en el dashboard
 */
public class RecentActivity {
    private String id;
    private String tipo;
    private String descripcion;
    private long timestamp;
    private String usuario;
    
    public RecentActivity() {}
    
    public RecentActivity(String id, String tipo, String descripcion, long timestamp, String usuario) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
        this.usuario = usuario;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}