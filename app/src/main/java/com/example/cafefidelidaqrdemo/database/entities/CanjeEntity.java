package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Canje según modelo ER lógico
 * Registra los canjes de beneficios realizados por clientes con OTP
 */
@Entity(
    tableName = "canjes",
    foreignKeys = {
        @ForeignKey(
            entity = BeneficioEntity.class,
            parentColumns = "id_beneficio",
            childColumns = "id_beneficio",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = ClienteEntity.class,
            parentColumns = "id_cliente",
            childColumns = "id_cliente",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = SucursalEntity.class,
            parentColumns = "id_sucursal",
            childColumns = "id_sucursal",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index(value = "id_beneficio"),
        @Index(value = "id_cliente"),
        @Index(value = "id_sucursal"),
        @Index(value = "otp_codigo"),
        @Index(value = "estado")
    }
)
public class CanjeEntity {
    @PrimaryKey
    @NonNull
    private String id_canje; // Identificador único del canje
    
    @NonNull
    private String id_beneficio; // FK a BeneficioEntity
    
    @NonNull
    private String id_cliente; // FK a ClienteEntity
    
    private String id_sucursal; // FK a SucursalEntity (puede ser null si es online)
    
    private String otp_codigo; // Código OTP de 6 dígitos
    private long otp_expiracion; // Timestamp de expiración del OTP (60 segundos)
    private boolean otp_usado; // Si el OTP ya fue utilizado
    
    @NonNull
    private String estado; // PENDIENTE, CANJEADO, EXPIRADO, CANCELADO
    
    private long fecha_solicitud; // Timestamp cuando se solicitó el canje
    private long fecha_canje; // Timestamp cuando se completó el canje (null si no se ha canjeado)
    
    private String cajero_id; // ID del cajero que validó el canje (opcional)
    private String notas; // Notas adicionales del canje
    
    // Campos para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public CanjeEntity() {}
    
    // Constructor para nuevo canje
    public CanjeEntity(@NonNull String id_canje, @NonNull String id_beneficio, 
                      @NonNull String id_cliente, String id_sucursal) {
        this.id_canje = id_canje;
        this.id_beneficio = id_beneficio;
        this.id_cliente = id_cliente;
        this.id_sucursal = id_sucursal;
        this.estado = "PENDIENTE";
        this.fecha_solicitud = System.currentTimeMillis();
        this.otp_usado = false;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_canje() { return id_canje; }
    public void setId_canje(@NonNull String id_canje) { this.id_canje = id_canje; }
    
    @NonNull
    public String getId_beneficio() { return id_beneficio; }
    public void setId_beneficio(@NonNull String id_beneficio) { this.id_beneficio = id_beneficio; }
    
    @NonNull
    public String getId_cliente() { return id_cliente; }
    public void setId_cliente(@NonNull String id_cliente) { this.id_cliente = id_cliente; }
    
    public String getId_sucursal() { return id_sucursal; }
    public void setId_sucursal(String id_sucursal) { this.id_sucursal = id_sucursal; }
    
    public String getOtp_codigo() { return otp_codigo; }
    public void setOtp_codigo(String otp_codigo) { this.otp_codigo = otp_codigo; }
    
    public long getOtp_expiracion() { return otp_expiracion; }
    public void setOtp_expiracion(long otp_expiracion) { this.otp_expiracion = otp_expiracion; }
    
    public boolean isOtp_usado() { return otp_usado; }
    public void setOtp_usado(boolean otp_usado) { this.otp_usado = otp_usado; }
    
    @NonNull
    public String getEstado() { return estado; }
    public void setEstado(@NonNull String estado) { this.estado = estado; }
    
    public long getFecha_solicitud() { return fecha_solicitud; }
    public void setFecha_solicitud(long fecha_solicitud) { this.fecha_solicitud = fecha_solicitud; }
    
    public long getFecha_canje() { return fecha_canje; }
    public void setFecha_canje(long fecha_canje) { this.fecha_canje = fecha_canje; }
    
    public String getCajero_id() { return cajero_id; }
    public void setCajero_id(String cajero_id) { this.cajero_id = cajero_id; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad
    public boolean isOtpValido() {
        return otp_codigo != null && !otp_usado && 
               System.currentTimeMillis() < otp_expiracion;
    }
    
    public boolean isCanjeado() {
        return "CANJEADO".equals(estado);
    }
    
    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }
    
    public long getTiempoRestanteOtp() {
        if (otp_expiracion == 0) return 0;
        long restante = otp_expiracion - System.currentTimeMillis();
        return Math.max(0, restante);
    }
}