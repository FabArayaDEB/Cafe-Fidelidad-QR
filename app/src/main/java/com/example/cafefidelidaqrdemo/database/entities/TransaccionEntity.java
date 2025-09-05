package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para cache offline de transacciones
 */
@Entity(tableName = "transacciones")
public class TransaccionEntity {
    @PrimaryKey
    @NonNull
    private String id;
    
    private String userId;
    private String sucursalId;
    private String mesaId;
    private String tipo;
    private double monto;
    private int puntos;
    private long fecha;
    private long qrTimestamp;
    private String hash;
    private boolean jwtUsed;
    private String descripcion;
    private long lastSync;
    private boolean needsSync;
    private boolean synced; // Indica si ya fue sincronizada con Firebase
    
    // Constructor vacío requerido por Room
    public TransaccionEntity() {}
    
    // Constructor completo
    public TransaccionEntity(@NonNull String id, String userId, String sucursalId, 
                           String mesaId, String tipo, double monto, int puntos, 
                           long fecha, long qrTimestamp, String hash, boolean jwtUsed, 
                           String descripcion) {
        this.id = id;
        this.userId = userId;
        this.sucursalId = sucursalId;
        this.mesaId = mesaId;
        this.tipo = tipo;
        this.monto = monto;
        this.puntos = puntos;
        this.fecha = fecha;
        this.qrTimestamp = qrTimestamp;
        this.hash = hash;
        this.jwtUsed = jwtUsed;
        this.descripcion = descripcion;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
    
    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    
    public long getQrTimestamp() { return qrTimestamp; }
    public void setQrTimestamp(long qrTimestamp) { this.qrTimestamp = qrTimestamp; }
    
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public boolean isJwtUsed() { return jwtUsed; }
    public void setJwtUsed(boolean jwtUsed) { this.jwtUsed = jwtUsed; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}