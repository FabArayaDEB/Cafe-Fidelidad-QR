package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Canje según modelo ER lógico
 * Representa cuando un cliente usa su beneficio
 */
@Entity(tableName = "canjes")
public class CanjeEntity {
    @PrimaryKey
    @NonNull
    private String id_canje; // Identificador único del canje
    
    private String id_cliente; // Cliente que realizó el canje
    private String id_beneficio; // Beneficio canjeado
    private String id_sucursal; // Dónde se realizó el canje
    private long fecha_hora; // Momento del canje
    private String codigo_otp; // Código de un solo uso (para validación en caja)
    private String estado_sync; // Igual que en visitas, controla sincronización: PENDIENTE, ENVIADO, ERROR
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public CanjeEntity() {}
    
    // Constructor completo
    public CanjeEntity(@NonNull String id_canje, String id_cliente, String id_beneficio, 
                      String id_sucursal, long fecha_hora, String codigo_otp, String estado_sync) {
        this.id_canje = id_canje;
        this.id_cliente = id_cliente;
        this.id_beneficio = id_beneficio;
        this.id_sucursal = id_sucursal;
        this.fecha_hora = fecha_hora;
        this.codigo_otp = codigo_otp;
        this.estado_sync = estado_sync;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_canje() { return id_canje; }
    public void setId_canje(@NonNull String id_canje) { this.id_canje = id_canje; }
    
    public String getId_cliente() { return id_cliente; }
    public void setId_cliente(String id_cliente) { this.id_cliente = id_cliente; }
    
    public String getId_beneficio() { return id_beneficio; }
    public void setId_beneficio(String id_beneficio) { this.id_beneficio = id_beneficio; }
    
    public String getId_sucursal() { return id_sucursal; }
    public void setId_sucursal(String id_sucursal) { this.id_sucursal = id_sucursal; }
    
    public long getFecha_hora() { return fecha_hora; }
    public void setFecha_hora(long fecha_hora) { this.fecha_hora = fecha_hora; }
    
    public String getCodigo_otp() { return codigo_otp; }
    public void setCodigo_otp(String codigo_otp) { this.codigo_otp = codigo_otp; }
    
    public String getEstado_sync() { return estado_sync; }
    public void setEstado_sync(String estado_sync) { this.estado_sync = estado_sync; }
    
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
     * Obtiene la fecha formateada como string
     */
    public String getFechaFormateada() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new java.util.Date(fecha_hora));
    }
    
    /**
     * Verifica si el canje es del día actual
     */
    public boolean isHoy() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hoyDia = cal.get(java.util.Calendar.DAY_OF_YEAR);
        int hoyAno = cal.get(java.util.Calendar.YEAR);
        
        cal.setTimeInMillis(fecha_hora);
        int canjeDia = cal.get(java.util.Calendar.DAY_OF_YEAR);
        int canjeAno = cal.get(java.util.Calendar.YEAR);
        
        return hoyDia == canjeDia && hoyAno == canjeAno;
    }
    
    /**
     * Verifica si el código OTP es válido (no nulo y no vacío)
     */
    public boolean hasValidOTP() {
        return codigo_otp != null && !codigo_otp.trim().isEmpty();
    }
    
    /**
     * Genera un nuevo código OTP de 6 dígitos
     */
    public void generarNuevoOTP() {
        java.util.Random random = new java.util.Random();
        int otp = 100000 + random.nextInt(900000); // Genera número entre 100000 y 999999
        this.codigo_otp = String.valueOf(otp);
        this.needsSync = true;
    }
}