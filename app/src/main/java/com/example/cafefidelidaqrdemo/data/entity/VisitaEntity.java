package com.example.cafefidelidaqrdemo.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import com.example.cafefidelidaqrdemo.data.converter.DateConverter;
import java.util.Date;

/**
 * Entidad para registrar visitas a sucursales mediante escaneo de QR
 * Maneja estados de sincronización para funcionamiento offline
 */
@Entity(tableName = "visitas")
@TypeConverters({DateConverter.class})
public class VisitaEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "sucursal_id")
    private String sucursalId;
    
    @ColumnInfo(name = "hash_qr")
    private String hashQr;
    
    @ColumnInfo(name = "timestamp_qr")
    private long timestampQr;
    
    @ColumnInfo(name = "nonce")
    private String nonce;
    
    @ColumnInfo(name = "firma")
    private String firma;
    
    @ColumnInfo(name = "fecha_escaneo")
    private Date fechaEscaneo;
    
    @ColumnInfo(name = "estado_sincronizacion")
    private EstadoSincronizacion estadoSincronizacion;
    
    @ColumnInfo(name = "fecha_sincronizacion")
    private Date fechaSincronizacion;
    
    @ColumnInfo(name = "intentos_sincronizacion")
    private int intentosSincronizacion;
    
    @ColumnInfo(name = "error_sincronizacion")
    private String errorSincronizacion;
    
    @ColumnInfo(name = "progreso_actualizado")
    private String progresoActualizado;
    
    // Constructores
    public VisitaEntity() {
        this.fechaEscaneo = new Date();
        this.estadoSincronizacion = EstadoSincronizacion.PENDIENTE;
        this.intentosSincronizacion = 0;
    }
    
    public VisitaEntity(String sucursalId, String hashQr, long timestampQr, 
                       String nonce, String firma) {
        this();
        this.sucursalId = sucursalId;
        this.hashQr = hashQr;
        this.timestampQr = timestampQr;
        this.nonce = nonce;
        this.firma = firma;
    }
    
    // Getters y Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getSucursalId() {
        return sucursalId;
    }
    
    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }
    
    public String getHashQr() {
        return hashQr;
    }
    
    public void setHashQr(String hashQr) {
        this.hashQr = hashQr;
    }
    
    public long getTimestampQr() {
        return timestampQr;
    }
    
    public void setTimestampQr(long timestampQr) {
        this.timestampQr = timestampQr;
    }
    
    public String getNonce() {
        return nonce;
    }
    
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    
    public String getFirma() {
        return firma;
    }
    
    public void setFirma(String firma) {
        this.firma = firma;
    }
    
    public Date getFechaEscaneo() {
        return fechaEscaneo;
    }
    
    public void setFechaEscaneo(Date fechaEscaneo) {
        this.fechaEscaneo = fechaEscaneo;
    }
    
    public EstadoSincronizacion getEstadoSincronizacion() {
        return estadoSincronizacion;
    }
    
    public void setEstadoSincronizacion(EstadoSincronizacion estadoSincronizacion) {
        this.estadoSincronizacion = estadoSincronizacion;
    }
    
    public Date getFechaSincronizacion() {
        return fechaSincronizacion;
    }
    
    public void setFechaSincronizacion(Date fechaSincronizacion) {
        this.fechaSincronizacion = fechaSincronizacion;
    }
    
    public int getIntentosSincronizacion() {
        return intentosSincronizacion;
    }
    
    public void setIntentosSincronizacion(int intentosSincronizacion) {
        this.intentosSincronizacion = intentosSincronizacion;
    }
    
    public String getErrorSincronizacion() {
        return errorSincronizacion;
    }
    
    public void setErrorSincronizacion(String errorSincronizacion) {
        this.errorSincronizacion = errorSincronizacion;
    }
    
    public String getProgresoActualizado() {
        return progresoActualizado;
    }
    
    public void setProgresoActualizado(String progresoActualizado) {
        this.progresoActualizado = progresoActualizado;
    }
    
    // Métodos de utilidad
    public boolean esPendienteSincronizacion() {
        return estadoSincronizacion == EstadoSincronizacion.PENDIENTE;
    }
    
    public boolean estaSincronizada() {
        return estadoSincronizacion == EstadoSincronizacion.ENVIADA;
    }
    
    public boolean tieneError() {
        return estadoSincronizacion == EstadoSincronizacion.ERROR;
    }
    
    public void marcarComoEnviada(String progreso) {
        this.estadoSincronizacion = EstadoSincronizacion.ENVIADA;
        this.fechaSincronizacion = new Date();
        this.progresoActualizado = progreso;
        this.errorSincronizacion = null;
    }
    
    public void marcarComoError(String error) {
        this.estadoSincronizacion = EstadoSincronizacion.ERROR;
        this.intentosSincronizacion++;
        this.errorSincronizacion = error;
    }
    
    public void reintentar() {
        this.estadoSincronizacion = EstadoSincronizacion.PENDIENTE;
        this.errorSincronizacion = null;
    }
    
    /**
     * Estados de sincronización de la visita
     */
    public enum EstadoSincronizacion {
        PENDIENTE,  // Esperando ser enviada al servidor
        ENVIADA,    // Enviada exitosamente al servidor
        ERROR       // Error al enviar al servidor
    }
}