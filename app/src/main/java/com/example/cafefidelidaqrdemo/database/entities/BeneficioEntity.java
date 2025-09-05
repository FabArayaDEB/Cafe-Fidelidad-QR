package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Beneficio según modelo ER lógico
 * Define qué se gana y cómo cuando se cumplen las condiciones
 */
@Entity(tableName = "beneficios")
public class BeneficioEntity {
    @PrimaryKey
    @NonNull
    private String id_beneficio; // Identificador único del beneficio
    
    private String nombre; // Título (ej. "Café gratis cada 5 visitas")
    private String tipo; // Define el beneficio: DESCUENTO, 2x1, PREMIO
    private String regla; // JSON o cadena que define la condición (ej. { "cadaNVisitas":5 })
    private int requisito_visitas; // Número mínimo de visitas para habilitarlo
    private double descuento_pct; // Porcentaje de descuento aplicable
    private double descuento_monto; // Monto fijo de descuento aplicable
    private String producto_premio; // ID del producto gratuito o especial
    private long vigencia_ini; // Fecha de inicio de validez del beneficio
    private long vigencia_fin; // Fecha de fin de validez del beneficio
    private String estado; // Activo/inactivo/expirado
    private String sucursales_aplicables; // JSON con IDs de sucursales donde aplica
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public BeneficioEntity() {}
    
    // Constructor completo
    public BeneficioEntity(@NonNull String id_beneficio, String nombre, String tipo, 
                          String regla, int requisito_visitas, double descuento_pct, 
                          double descuento_monto, String producto_premio, 
                          long vigencia_ini, long vigencia_fin, String estado, 
                          String sucursales_aplicables) {
        this.id_beneficio = id_beneficio;
        this.nombre = nombre;
        this.tipo = tipo;
        this.regla = regla;
        this.requisito_visitas = requisito_visitas;
        this.descuento_pct = descuento_pct;
        this.descuento_monto = descuento_monto;
        this.producto_premio = producto_premio;
        this.vigencia_ini = vigencia_ini;
        this.vigencia_fin = vigencia_fin;
        this.estado = estado;
        this.sucursales_aplicables = sucursales_aplicables;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_beneficio() { return id_beneficio; }
    public void setId_beneficio(@NonNull String id_beneficio) { this.id_beneficio = id_beneficio; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getRegla() { return regla; }
    public void setRegla(String regla) { this.regla = regla; }
    
    public int getRequisito_visitas() { return requisito_visitas; }
    public void setRequisito_visitas(int requisito_visitas) { this.requisito_visitas = requisito_visitas; }
    
    public double getDescuento_pct() { return descuento_pct; }
    public void setDescuento_pct(double descuento_pct) { this.descuento_pct = descuento_pct; }
    
    public double getDescuento_monto() { return descuento_monto; }
    public void setDescuento_monto(double descuento_monto) { this.descuento_monto = descuento_monto; }
    
    public String getProducto_premio() { return producto_premio; }
    public void setProducto_premio(String producto_premio) { this.producto_premio = producto_premio; }
    
    public long getVigencia_ini() { return vigencia_ini; }
    public void setVigencia_ini(long vigencia_ini) { this.vigencia_ini = vigencia_ini; }
    
    public long getVigencia_fin() { return vigencia_fin; }
    public void setVigencia_fin(long vigencia_fin) { this.vigencia_fin = vigencia_fin; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getSucursales_aplicables() { return sucursales_aplicables; }
    public void setSucursales_aplicables(String sucursales_aplicables) { this.sucursales_aplicables = sucursales_aplicables; }
    
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
    
    public boolean isInactivo() {
        return "inactivo".equalsIgnoreCase(estado);
    }
    
    public boolean isExpirado() {
        return "expirado".equalsIgnoreCase(estado);
    }
    
    public void activar() {
        this.estado = "activo";
        this.needsSync = true;
    }
    
    public void desactivar() {
        this.estado = "inactivo";
        this.needsSync = true;
    }
    
    public void expirar() {
        this.estado = "expirado";
        this.needsSync = true;
    }
    
    /**
     * Verifica si el beneficio está vigente en la fecha actual
     */
    public boolean isVigente() {
        long ahora = System.currentTimeMillis();
        return ahora >= vigencia_ini && ahora <= vigencia_fin && isActivo();
    }
    
    /**
     * Verifica si es un beneficio de descuento
     */
    public boolean isDescuento() {
        return "DESCUENTO".equalsIgnoreCase(tipo);
    }
    
    /**
     * Verifica si es un beneficio 2x1
     */
    public boolean is2x1() {
        return "2x1".equalsIgnoreCase(tipo);
    }
    
    /**
     * Verifica si es un beneficio de premio
     */
    public boolean isPremio() {
        return "PREMIO".equalsIgnoreCase(tipo);
    }
    
    /**
     * Obtiene el descuento formateado como string
     */
    public String getDescuentoFormateado() {
        if (descuento_pct > 0) {
            return String.format("%.0f%% de descuento", descuento_pct);
        } else if (descuento_monto > 0) {
            return String.format("$%.2f de descuento", descuento_monto);
        }
        return "Sin descuento";
    }
}