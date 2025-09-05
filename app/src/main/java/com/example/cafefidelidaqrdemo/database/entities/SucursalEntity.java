package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Sucursal según modelo ER lógico
 * Permite saber dónde se registran las visitas y canjes
 */
@Entity(tableName = "sucursales")
public class SucursalEntity {
    @PrimaryKey
    @NonNull
    private String id_sucursal; // Identificador único de la cafetería/sucursal
    
    private String nombre; // Nombre de la sucursal (ej. "Café Centro")
    private String direccion; // Dirección física
    private double lat; // Latitud - ubicación geográfica
    private double lon; // Longitud - ubicación geográfica
    private String horario; // Horarios de atención
    private String estado; // Activo/inactivo (si la sucursal está operativa)
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public SucursalEntity() {}
    
    // Constructor completo
    public SucursalEntity(@NonNull String id_sucursal, String nombre, String direccion, 
                         double lat, double lon, String horario, String estado) {
        this.id_sucursal = id_sucursal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.lat = lat;
        this.lon = lon;
        this.horario = horario;
        this.estado = estado;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_sucursal() { return id_sucursal; }
    public void setId_sucursal(@NonNull String id_sucursal) { this.id_sucursal = id_sucursal; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
    
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad
    public boolean isActiva() {
        return "activo".equalsIgnoreCase(estado);
    }
    
    public boolean isInactiva() {
        return "inactivo".equalsIgnoreCase(estado);
    }
    
    public void activar() {
        this.estado = "activo";
        this.needsSync = true;
    }
    
    public void desactivar() {
        this.estado = "inactivo";
        this.needsSync = true;
    }
    
    /**
     * Calcula la distancia en metros entre esta sucursal y una ubicación dada
     */
    public double calcularDistancia(double latitud, double longitud) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(latitud - this.lat);
        double lonDistance = Math.toRadians(longitud - this.lon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(latitud))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Convertir a metros
        
        return distance;
    }
}