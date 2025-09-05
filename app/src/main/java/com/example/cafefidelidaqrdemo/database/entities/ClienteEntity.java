package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Cliente según modelo ER lógico
 * Es el actor principal que acumula visitas y canjea beneficios
 */
@Entity(tableName = "clientes")
public class ClienteEntity {
    @PrimaryKey
    @NonNull
    private String id_cliente; // Identificador único del cliente
    
    private String nombre; // Nombre completo del cliente
    private String email; // Usado como credencial de login y comunicación
    private String telefono; // Contacto opcional para notificaciones o recuperación
    private String fecha_nac; // Permite personalizar beneficios (ej. cumpleaños)
    private String estado; // Indica si el cliente está activo/inactivo/bloqueado
    private long creado_en; // Fecha en que el cliente fue registrado en el sistema
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public ClienteEntity() {}
    
    // Constructor completo
    public ClienteEntity(@NonNull String id_cliente, String nombre, String email, 
                        String telefono, String fecha_nac, String estado) {
        this.id_cliente = id_cliente;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fecha_nac = fecha_nac;
        this.estado = estado;
        this.creado_en = System.currentTimeMillis();
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_cliente() { return id_cliente; }
    public void setId_cliente(@NonNull String id_cliente) { this.id_cliente = id_cliente; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getFecha_nac() { return fecha_nac; }
    public void setFecha_nac(String fecha_nac) { this.fecha_nac = fecha_nac; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public long getCreado_en() { return creado_en; }
    public void setCreado_en(long creado_en) { this.creado_en = creado_en; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad
    public boolean isActivo() {
        return "activo".equalsIgnoreCase(estado);
    }
    
    public boolean isBloqueado() {
        return "bloqueado".equalsIgnoreCase(estado);
    }
    
    public void activar() {
        this.estado = "activo";
        this.needsSync = true;
    }
    
    public void bloquear() {
        this.estado = "bloqueado";
        this.needsSync = true;
    }
    
    public void desactivar() {
        this.estado = "inactivo";
        this.needsSync = true;
    }
}