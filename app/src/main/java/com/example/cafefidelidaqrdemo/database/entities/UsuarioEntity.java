package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para cache offline de datos de usuario
 */
@Entity(tableName = "usuarios")
public class UsuarioEntity {
    @PrimaryKey
    @NonNull
    private String uid;
    
    private String names;
    private String email;
    private String proveedor;
    private String estado;
    private String imagen;
    private long date;
    private int puntos;
    private String nivel;
    private double totalCompras;
    private long ultimaVisita;
    private String telefono;
    private String fechaNacimiento;
    private long lastSync; // Timestamp de última sincronización
    private boolean needsSync; // Indica si necesita sincronización
    
    // Constructor vacío requerido por Room
    public UsuarioEntity() {}
    
    // Constructor completo
    public UsuarioEntity(@NonNull String uid, String names, String email, String proveedor,
                        String estado, String imagen, long date, int puntos, String nivel,
                        double totalCompras, long ultimaVisita, String telefono, 
                        String fechaNacimiento) {
        this.uid = uid;
        this.names = names;
        this.email = email;
        this.proveedor = proveedor;
        this.estado = estado;
        this.imagen = imagen;
        this.date = date;
        this.puntos = puntos;
        this.nivel = nivel;
        this.totalCompras = totalCompras;
        this.ultimaVisita = ultimaVisita;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getUid() { return uid; }
    public void setUid(@NonNull String uid) { this.uid = uid; }
    
    public String getNames() { return names; }
    public void setNames(String names) { this.names = names; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
    
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    
    public double getTotalCompras() { return totalCompras; }
    public void setTotalCompras(double totalCompras) { this.totalCompras = totalCompras; }
    
    public long getUltimaVisita() { return ultimaVisita; }
    public void setUltimaVisita(long ultimaVisita) { this.ultimaVisita = ultimaVisita; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
}