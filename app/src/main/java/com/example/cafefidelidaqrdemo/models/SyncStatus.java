package com.example.cafefidelidaqrdemo.models;

import java.util.Date;

/**
 * Modelo para representar el estado de sincronización
 */
public class SyncStatus {
    
    public enum Estado {
        IDLE,           // Sin actividad de sincronización
        SYNCING,        // Sincronizando actualmente
        SUCCESS,        // Última sincronización exitosa
        ERROR,          // Error en la última sincronización
        OFFLINE,        // Sin conexión
        PENDING         // Pendiente de sincronización
    }
    
    private Estado estado;
    private Date ultimaSincronizacion;
    private String mensaje;
    private String detalleError;
    private int intentosRealizados;
    private int intentosMaximos;
    private long tiempoProximoIntento;
    private boolean requiereSincronizacion;
    private boolean conexionDisponible;
    private int elementosPendientes;
    private int elementosSincronizados;
    private double progreso; // 0.0 - 1.0
    
    // Constructor vacío
    public SyncStatus() {
        this.estado = Estado.IDLE;
        this.ultimaSincronizacion = null;
        this.mensaje = "";
        this.detalleError = null;
        this.intentosRealizados = 0;
        this.intentosMaximos = 3;
        this.tiempoProximoIntento = 0;
        this.requiereSincronizacion = false;
        this.conexionDisponible = true;
        this.elementosPendientes = 0;
        this.elementosSincronizados = 0;
        this.progreso = 0.0;
    }
    
    // Constructor con estado
    public SyncStatus(Estado estado, String mensaje) {
        this();
        this.estado = estado;
        this.mensaje = mensaje;
    }
    
    // Constructor completo
    public SyncStatus(Estado estado, Date ultimaSincronizacion, String mensaje, 
                     String detalleError, int intentosRealizados, int intentosMaximos, 
                     long tiempoProximoIntento, boolean requiereSincronizacion, 
                     boolean conexionDisponible, int elementosPendientes, 
                     int elementosSincronizados, double progreso) {
        this.estado = estado;
        this.ultimaSincronizacion = ultimaSincronizacion;
        this.mensaje = mensaje;
        this.detalleError = detalleError;
        this.intentosRealizados = intentosRealizados;
        this.intentosMaximos = intentosMaximos;
        this.tiempoProximoIntento = tiempoProximoIntento;
        this.requiereSincronizacion = requiereSincronizacion;
        this.conexionDisponible = conexionDisponible;
        this.elementosPendientes = elementosPendientes;
        this.elementosSincronizados = elementosSincronizados;
        this.progreso = progreso;
    }
    
    // Getters y Setters
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    
    public Date getUltimaSincronizacion() { return ultimaSincronizacion; }
    public void setUltimaSincronizacion(Date ultimaSincronizacion) { this.ultimaSincronizacion = ultimaSincronizacion; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public String getDetalleError() { return detalleError; }
    public void setDetalleError(String detalleError) { this.detalleError = detalleError; }
    
    public int getIntentosRealizados() { return intentosRealizados; }
    public void setIntentosRealizados(int intentosRealizados) { this.intentosRealizados = intentosRealizados; }
    
    public int getIntentosMaximos() { return intentosMaximos; }
    public void setIntentosMaximos(int intentosMaximos) { this.intentosMaximos = intentosMaximos; }
    
    public long getTiempoProximoIntento() { return tiempoProximoIntento; }
    public void setTiempoProximoIntento(long tiempoProximoIntento) { this.tiempoProximoIntento = tiempoProximoIntento; }
    
    public boolean isRequiereSincronizacion() { return requiereSincronizacion; }
    public void setRequiereSincronizacion(boolean requiereSincronizacion) { this.requiereSincronizacion = requiereSincronizacion; }
    
    public boolean isConexionDisponible() { return conexionDisponible; }
    public void setConexionDisponible(boolean conexionDisponible) { this.conexionDisponible = conexionDisponible; }
    
    public int getElementosPendientes() { return elementosPendientes; }
    public void setElementosPendientes(int elementosPendientes) { this.elementosPendientes = elementosPendientes; }
    
    public int getElementosSincronizados() { return elementosSincronizados; }
    public void setElementosSincronizados(int elementosSincronizados) { this.elementosSincronizados = elementosSincronizados; }
    
    public double getProgreso() { return progreso; }
    public void setProgreso(double progreso) { this.progreso = Math.max(0.0, Math.min(1.0, progreso)); }
    
    // Métodos de utilidad
    public boolean estaSincronizando() {
        return estado == Estado.SYNCING;
    }
    
    public boolean tieneError() {
        return estado == Estado.ERROR;
    }
    
    public boolean estaOffline() {
        return estado == Estado.OFFLINE || !conexionDisponible;
    }
    
    public boolean puedeReintentar() {
        return intentosRealizados < intentosMaximos && conexionDisponible;
    }
    
    public boolean necesitaSincronizacion() {
        return requiereSincronizacion || elementosPendientes > 0;
    }
    
    public long getTiempoDesdeUltimaSincronizacion() {
        if (ultimaSincronizacion == null) return Long.MAX_VALUE;
        return System.currentTimeMillis() - ultimaSincronizacion.getTime();
    }
    
    public boolean esSincronizacionReciente() {
        return getTiempoDesdeUltimaSincronizacion() < 300000; // 5 minutos
    }
    
    public String getMensajeEstado() {
        switch (estado) {
            case IDLE:
                return "Listo";
            case SYNCING:
                return "Sincronizando...";
            case SUCCESS:
                return "Sincronizado";
            case ERROR:
                return "Error de sincronización";
            case OFFLINE:
                return "Sin conexión";
            case PENDING:
                return "Pendiente de sincronización";
            default:
                return "Estado desconocido";
        }
    }
    
    public String getDescripcionCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMensajeEstado());
        
        if (!mensaje.isEmpty()) {
            sb.append(": ").append(mensaje);
        }
        
        if (elementosPendientes > 0) {
            sb.append(" (").append(elementosPendientes).append(" pendientes)");
        }
        
        return sb.toString();
    }
    
    // Métodos para crear instancias comunes
    public static SyncStatus idle() {
        return new SyncStatus(Estado.IDLE, "Listo para sincronizar");
    }
    
    public static SyncStatus syncing(String mensaje) {
        return new SyncStatus(Estado.SYNCING, mensaje != null ? mensaje : "Sincronizando...");
    }
    
    public static SyncStatus success(Date ultimaSincronizacion) {
        SyncStatus status = new SyncStatus(Estado.SUCCESS, "Sincronización exitosa");
        status.setUltimaSincronizacion(ultimaSincronizacion);
        return status;
    }
    
    public static SyncStatus error(String detalleError) {
        SyncStatus status = new SyncStatus(Estado.ERROR, "Error en la sincronización");
        status.setDetalleError(detalleError);
        return status;
    }
    
    public static SyncStatus offline() {
        SyncStatus status = new SyncStatus(Estado.OFFLINE, "Sin conexión a internet");
        status.setConexionDisponible(false);
        return status;
    }
    
    public static SyncStatus pending(int elementosPendientes) {
        SyncStatus status = new SyncStatus(Estado.PENDING, "Pendiente de sincronización");
        status.setElementosPendientes(elementosPendientes);
        status.setRequiereSincronizacion(true);
        return status;
    }
    
    @Override
    public String toString() {
        return "SyncStatus{" +
                "estado=" + estado +
                ", ultimaSincronizacion=" + ultimaSincronizacion +
                ", mensaje='" + mensaje + '\'' +
                ", detalleError='" + detalleError + '\'' +
                ", intentosRealizados=" + intentosRealizados +
                ", elementosPendientes=" + elementosPendientes +
                ", progreso=" + progreso +
                '}';
    }
}