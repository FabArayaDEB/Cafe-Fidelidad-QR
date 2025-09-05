package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Regla según modelo ER lógico
 * Permite definir dinámicamente las condiciones de los beneficios sin cambiar el código
 */
@Entity(tableName = "reglas")
public class ReglaEntity {
    @PrimaryKey
    @NonNull
    private String id_regla; // Identificador único
    
    private String descripcion; // Texto explicativo de la regla (ej. "Cada 5 visitas un café gratis")
    private String expresion; // Lógica expresada en JSON o formato interpretable (ej. { "cadaNVisitas": 5 })
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public ReglaEntity() {}
    
    // Constructor completo
    public ReglaEntity(@NonNull String id_regla, String descripcion, String expresion) {
        this.id_regla = id_regla;
        this.descripcion = descripcion;
        this.expresion = expresion;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_regla() { return id_regla; }
    public void setId_regla(@NonNull String id_regla) { this.id_regla = id_regla; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getExpresion() { return expresion; }
    public void setExpresion(String expresion) { this.expresion = expresion; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad
    
    /**
     * Verifica si la expresión es válida (no nula y no vacía)
     */
    public boolean hasValidExpresion() {
        return expresion != null && !expresion.trim().isEmpty();
    }
    
    /**
     * Verifica si la expresión parece ser JSON válido
     */
    public boolean isJSONExpresion() {
        if (!hasValidExpresion()) return false;
        String trimmed = expresion.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }
    
    /**
     * Obtiene una descripción corta de la regla (máximo 50 caracteres)
     */
    public String getDescripcionCorta() {
        if (descripcion == null || descripcion.length() <= 50) {
            return descripcion;
        }
        return descripcion.substring(0, 47) + "...";
    }
    
    /**
     * Verifica si la regla contiene una palabra clave específica en la descripción
     */
    public boolean contieneEnDescripcion(String palabraClave) {
        if (descripcion == null || palabraClave == null) {
            return false;
        }
        return descripcion.toLowerCase().contains(palabraClave.toLowerCase());
    }
    
    /**
     * Verifica si la regla contiene una palabra clave específica en la expresión
     */
    public boolean contieneEnExpresion(String palabraClave) {
        if (expresion == null || palabraClave == null) {
            return false;
        }
        return expresion.toLowerCase().contains(palabraClave.toLowerCase());
    }
    
    /**
     * Actualiza la regla con nueva descripción y expresión
     */
    public void actualizar(String nuevaDescripcion, String nuevaExpresion) {
        this.descripcion = nuevaDescripcion;
        this.expresion = nuevaExpresion;
        this.needsSync = true;
        this.lastSync = System.currentTimeMillis();
    }
}