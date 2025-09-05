package com.example.cafefidelidaqrdemo.models;

import java.util.Date;

/**
 * Modelo unificado para items del historial (visitas y canjes)
 */
public class HistorialItem {
    
    public enum Tipo {
        VISITA, CANJE
    }
    
    private String id;
    private Tipo tipo;
    private Date fechaHora;
    private String estadoSync;
    private String sucursalId;
    private String sucursalNombre;
    
    // Campos específicos de visita
    private String origen;
    private String hashQr;
    
    // Campos específicos de canje
    private String beneficioId;
    private String beneficioNombre;
    private String codigoOtp;
    
    // Campos adicionales para UI
    private String descripcion;
    private String iconoEstado;
    private int colorEstado;
    
    public HistorialItem() {
    }
    
    public HistorialItem(String id, Tipo tipo, Date fechaHora, String estadoSync) {
        this.id = id;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
        this.estadoSync = estadoSync;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Tipo getTipo() {
        return tipo;
    }
    
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
    
    public Date getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public String getEstadoSync() {
        return estadoSync;
    }
    
    public void setEstadoSync(String estadoSync) {
        this.estadoSync = estadoSync;
    }
    
    public String getSucursalId() {
        return sucursalId;
    }
    
    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }
    
    public String getSucursalNombre() {
        return sucursalNombre;
    }
    
    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }
    
    public String getOrigen() {
        return origen;
    }
    
    public void setOrigen(String origen) {
        this.origen = origen;
    }
    
    public String getHashQr() {
        return hashQr;
    }
    
    public void setHashQr(String hashQr) {
        this.hashQr = hashQr;
    }
    
    public String getBeneficioId() {
        return beneficioId;
    }
    
    public void setBeneficioId(String beneficioId) {
        this.beneficioId = beneficioId;
    }
    
    public String getBeneficioNombre() {
        return beneficioNombre;
    }
    
    public void setBeneficioNombre(String beneficioNombre) {
        this.beneficioNombre = beneficioNombre;
    }
    
    public String getCodigoOtp() {
        return codigoOtp;
    }
    
    public void setCodigoOtp(String codigoOtp) {
        this.codigoOtp = codigoOtp;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getIconoEstado() {
        return iconoEstado;
    }
    
    public void setIconoEstado(String iconoEstado) {
        this.iconoEstado = iconoEstado;
    }
    
    public int getColorEstado() {
        return colorEstado;
    }
    
    public void setColorEstado(int colorEstado) {
        this.colorEstado = colorEstado;
    }
    
    // Métodos de utilidad
    
    /**
     * Retorna true si el item está pendiente de sincronización
     */
    public boolean isPendienteSync() {
        return "PENDIENTE".equals(estadoSync);
    }
    
    /**
     * Retorna true si el item fue enviado exitosamente
     */
    public boolean isEnviado() {
        return "ENVIADO".equals(estadoSync);
    }
    
    /**
     * Retorna true si hubo error en la sincronización
     */
    public boolean isError() {
        return "ERROR".equals(estadoSync);
    }
    
    /**
     * Retorna el título para mostrar en la UI
     */
    public String getTitulo() {
        if (tipo == Tipo.VISITA) {
            return "Visita";
        } else {
            return "Canje";
        }
    }
    
    /**
     * Retorna el subtítulo para mostrar en la UI
     */
    public String getSubtitulo() {
        if (tipo == Tipo.VISITA) {
            if (sucursalNombre != null) {
                return sucursalNombre;
            } else {
                return "Sucursal: " + sucursalId;
            }
        } else {
            if (beneficioNombre != null) {
                return beneficioNombre;
            } else {
                return "Beneficio: " + beneficioId;
            }
        }
    }
    
    /**
     * Retorna el texto del estado para mostrar en la UI
     */
    public String getTextoEstado() {
        switch (estadoSync) {
            case "PENDIENTE":
                return "Pendiente";
            case "ENVIADO":
                return "Sincronizado";
            case "ERROR":
                return "Error";
            default:
                return "Desconocido";
        }
    }
    
    /**
     * Retorna el icono del estado para mostrar en la UI
     */
    public String getIconoEstadoUI() {
        switch (estadoSync) {
            case "PENDIENTE":
                return "⏳";
            case "ENVIADO":
                return "✓";
            case "ERROR":
                return "⚠";
            default:
                return "?";
        }
    }
    
    /**
     * Retorna información adicional para mostrar en detalles
     */
    public String getInfoAdicional() {
        StringBuilder info = new StringBuilder();
        
        if (tipo == Tipo.VISITA) {
            if (origen != null) {
                info.append("Origen: ").append(origen).append("\n");
            }
            if (hashQr != null) {
                info.append("QR: ").append(hashQr.substring(0, Math.min(8, hashQr.length()))).append("...");
            }
        } else {
            if (codigoOtp != null) {
                info.append("Código OTP: ").append(codigoOtp);
            }
        }
        
        return info.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        HistorialItem that = (HistorialItem) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "HistorialItem{" +
                "id='" + id + '\'' +
                ", tipo=" + tipo +
                ", fechaHora=" + fechaHora +
                ", estadoSync='" + estadoSync + '\'' +
                ", sucursalId='" + sucursalId + '\'' +
                '}';
    }
}