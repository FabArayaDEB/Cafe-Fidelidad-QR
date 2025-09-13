package com.example.cafefidelidaqrdemo.data.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Modelo de datos para transacciones
 */
public class Transaccion implements Serializable {
    
    private String id;
    private String userId;
    private String sucursalId;
    private String mesaId;
    private String tipo;
    private double monto;
    private int puntos;
    private Date fecha;
    private long qrTimestamp;
    private String hash;
    private boolean jwtUsed;
    private String descripcion;
    private Date lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío
    public Transaccion() {
        this.fecha = new Date();
        this.lastSync = new Date();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Constructor completo
    public Transaccion(String id, String userId, String sucursalId, String mesaId, 
                      String tipo, double monto, int puntos, Date fecha, 
                      long qrTimestamp, String hash, boolean jwtUsed, String descripcion) {
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
        this.lastSync = new Date();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
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
    
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    
    public long getQrTimestamp() { return qrTimestamp; }
    public void setQrTimestamp(long qrTimestamp) { this.qrTimestamp = qrTimestamp; }
    
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public boolean isJwtUsed() { return jwtUsed; }
    public void setJwtUsed(boolean jwtUsed) { this.jwtUsed = jwtUsed; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public Date getLastSync() { return lastSync; }
    public void setLastSync(Date lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad
    public boolean isPendienteSync() {
        return needsSync && !synced;
    }
    
    public void markAsSynced() {
        this.synced = true;
        this.needsSync = false;
        this.lastSync = new Date();
    }
    
    public void markAsNeedsSync() {
        this.needsSync = true;
        this.synced = false;
    }
    
    @Override
    public String toString() {
        return "Transaccion{" +
                "id='" + id + '\'' +
                ", tipo='" + tipo + '\'' +
                ", monto=" + monto +
                ", puntos=" + puntos +
                ", fecha=" + fecha +
                ", synced=" + synced +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Transaccion that = (Transaccion) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}