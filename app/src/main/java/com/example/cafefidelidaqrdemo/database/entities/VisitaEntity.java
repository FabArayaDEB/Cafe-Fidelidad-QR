package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Visita según modelo ER lógico
 * Es el registro básico que permite acumular puntos o visitas
 */
@Entity(tableName = "visitas")
public class VisitaEntity {
    @PrimaryKey
    @NonNull
    private String id_visita; // Identificador único de la visita
    
    private String id_cliente; // Cliente que realizó la visita
    private String id_sucursal; // Sucursal visitada
    private long fecha_hora; // Momento en que se escaneó el QR
    private String origen; // Fuente de registro (ej. "QR")
    private String estado_sync; // Si está sincronizado con el servidor: PENDIENTE, ENVIADO, ERROR
    private String hash_qr; // Huella digital del QR leído (seguridad anti-fraude)
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public VisitaEntity() {}
    
    // Constructor completo
    public VisitaEntity(@NonNull String id_visita, String id_cliente, String id_sucursal, 
                       long fecha_hora, String origen, String estado_sync, String hash_qr) {
        this.id_visita = id_visita;
        this.id_cliente = id_cliente;
        this.id_sucursal = id_sucursal;
        this.fecha_hora = fecha_hora;
        this.origen = origen;
        this.estado_sync = estado_sync;
        this.hash_qr = hash_qr;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_visita() { return id_visita; }
    public void setId_visita(@NonNull String id_visita) { this.id_visita = id_visita; }
    
    public String getId_cliente() { return id_cliente; }
    public void setId_cliente(String id_cliente) { this.id_cliente = id_cliente; }
    
    public String getId_sucursal() { return id_sucursal; }
    public void setId_sucursal(String id_sucursal) { this.id_sucursal = id_sucursal; }
    
    public long getFecha_hora() { return fecha_hora; }
    public void setFecha_hora(long fecha_hora) { this.fecha_hora = fecha_hora; }
    
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    
    public String getEstado_sync() { return estado_sync; }
    public void setEstado_sync(String estado_sync) { this.estado_sync = estado_sync; }
    
    public String getHash_qr() { return hash_qr; }
    public void setHash_qr(String hash_qr) { this.hash_qr = hash_qr; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad para estado de sincronización
    public boolean isPendiente() {
        return "PENDIENTE".equalsIgnoreCase(estado_sync);
    }
    
    public boolean isEnviado() {
        return "ENVIADO".equalsIgnoreCase(estado_sync);
    }
    
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(estado_sync);
    }
    
    public void marcarPendiente() {
        this.estado_sync = "PENDIENTE";
        this.needsSync = true;
        this.synced = false;
    }
    
    public void marcarEnviado() {
        this.estado_sync = "ENVIADO";
        this.needsSync = false;
        this.synced = true;
        this.lastSync = System.currentTimeMillis();
    }
    
    public void marcarError() {
        this.estado_sync = "ERROR";
        this.needsSync = true;
        this.synced = false;
    }
    
    /**
     * Verifica si la visita es del origen QR
     */
    public boolean isOrigenQR() {
        return "QR".equalsIgnoreCase(origen);
    }
    
    /**
     * Obtiene la fecha formateada como string
     */
    public String getFechaFormateada() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new java.util.Date(fecha_hora));
    }
    
    /**
     * Verifica si la visita es del día actual
     */
    public boolean isHoy() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hoyDia = cal.get(java.util.Calendar.DAY_OF_YEAR);
        int hoyAno = cal.get(java.util.Calendar.YEAR);
        
        cal.setTimeInMillis(fecha_hora);
        int visitaDia = cal.get(java.util.Calendar.DAY_OF_YEAR);
        int visitaAno = cal.get(java.util.Calendar.YEAR);
        
        return hoyDia == visitaDia && hoyAno == visitaAno;
    }
}