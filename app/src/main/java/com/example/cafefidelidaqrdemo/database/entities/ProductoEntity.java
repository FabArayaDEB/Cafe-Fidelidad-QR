package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entidad Room para Producto según modelo ER lógico
 * Se utiliza en beneficios (ej. "café gratis" o "2x1 en pasteles")
 */
@Entity(tableName = "productos")
public class ProductoEntity {
    @PrimaryKey
    @NonNull
    private String id_producto; // Identificador único del producto
    
    private String nombre; // Nombre del producto (ej. "Capuccino")
    private String categoria; // Clasificación (ej. bebida caliente, pastelería, etc.)
    private String descripcion; // Descripción del producto
    private double precio; // Precio de venta
    private String estado; // Disponible/no disponible
    
    // Campos adicionales para sincronización offline
    private long lastSync;
    private boolean needsSync;
    private boolean synced;
    
    // Constructor vacío requerido por Room
    public ProductoEntity() {}
    
    // Constructor completo
    public ProductoEntity(@NonNull String id_producto, String nombre, String categoria, 
                         String descripcion, double precio, String estado) {
        this.id_producto = id_producto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
        this.estado = estado;
        this.lastSync = System.currentTimeMillis();
        this.needsSync = true;
        this.synced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getId_producto() { return id_producto; }
    public void setId_producto(@NonNull String id_producto) { this.id_producto = id_producto; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
    
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }
    
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
    
    // Métodos de utilidad
    public boolean isDisponible() {
        return "disponible".equalsIgnoreCase(estado);
    }
    
    public boolean isNoDisponible() {
        return "no disponible".equalsIgnoreCase(estado);
    }
    
    public void marcarDisponible() {
        this.estado = "disponible";
        this.needsSync = true;
    }
    
    public void marcarNoDisponible() {
        this.estado = "no disponible";
        this.needsSync = true;
    }
    
    /**
     * Verifica si el producto pertenece a una categoría específica
     */
    public boolean perteneceACategoria(String categoriaFiltro) {
        if (categoriaFiltro == null || categoriaFiltro.trim().isEmpty()) {
            return true;
        }
        return this.categoria != null && 
               this.categoria.toLowerCase().contains(categoriaFiltro.toLowerCase());
    }
    
    /**
     * Obtiene el precio formateado como string
     */
    public String getPrecioFormateado() {
        return String.format("$%.2f", precio);
    }
}