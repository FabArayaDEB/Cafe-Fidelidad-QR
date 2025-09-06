package com.example.cafefidelidaqrdemo.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad para gestionar productos del catálogo
 * Utilizada en el módulo de administración para CRUD de productos
 */
@Entity(tableName = "productos")
public class ProductoEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "nombre")
    private String nombre;
    
    @ColumnInfo(name = "descripcion")
    private String descripcion;
    
    @ColumnInfo(name = "precio")
    private double precio;
    
    @ColumnInfo(name = "categoria")
    private String categoria;
    
    @ColumnInfo(name = "imagen_url")
    private String imagenUrl;
    
    @ColumnInfo(name = "codigo_barras")
    private String codigoBarras;
    
    @ColumnInfo(name = "stock_disponible")
    private int stockDisponible;
    
    @ColumnInfo(name = "activo")
    private boolean activo;
    
    @ColumnInfo(name = "fecha_creacion")
    private long fechaCreacion;
    
    @ColumnInfo(name = "fecha_modificacion")
    private long fechaModificacion;
    
    @ColumnInfo(name = "version")
    private int version; // Para control de versiones en edición simultánea
    
    @ColumnInfo(name = "creado_por")
    private String creadoPor;
    
    @ColumnInfo(name = "modificado_por")
    private String modificadoPor;
    
    // Constructor vacío requerido por Room
    public ProductoEntity() {
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaModificacion = System.currentTimeMillis();
        this.activo = true;
        this.version = 1;
    }
    
    // Constructor completo
    public ProductoEntity(String nombre, String descripcion, double precio, 
                         String categoria, String imagenUrl, String codigoBarras, 
                         int stockDisponible, String creadoPor) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
        this.codigoBarras = codigoBarras;
        this.stockDisponible = stockDisponible;
        this.creadoPor = creadoPor;
        this.modificadoPor = creadoPor;
    }
    
    // Getters y Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
        updateModificationTime();
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        updateModificationTime();
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
        updateModificationTime();
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
        updateModificationTime();
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
        updateModificationTime();
    }
    
    public String getCodigoBarras() {
        return codigoBarras;
    }
    
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
        updateModificationTime();
    }
    
    public int getStockDisponible() {
        return stockDisponible;
    }
    
    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
        updateModificationTime();
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
        updateModificationTime();
    }
    
    public long getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public long getFechaModificacion() {
        return fechaModificacion;
    }
    
    public void setFechaModificacion(long fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getCreadoPor() {
        return creadoPor;
    }
    
    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
    
    public String getModificadoPor() {
        return modificadoPor;
    }
    
    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    // Métodos de utilidad
    private void updateModificationTime() {
        this.fechaModificacion = System.currentTimeMillis();
        this.version++;
    }
    
    /**
     * Desactiva el producto (eliminación lógica)
     */
    public void desactivar(String modificadoPor) {
        this.activo = false;
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    /**
     * Activa el producto
     */
    public void activar(String modificadoPor) {
        this.activo = true;
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    /**
     * Verifica si el producto tiene stock disponible
     */
    public boolean tieneStock() {
        return stockDisponible > 0;
    }
    
    /**
     * Reduce el stock del producto
     */
    public boolean reducirStock(int cantidad) {
        if (stockDisponible >= cantidad) {
            stockDisponible -= cantidad;
            updateModificationTime();
            return true;
        }
        return false;
    }
    
    /**
     * Aumenta el stock del producto
     */
    public void aumentarStock(int cantidad) {
        stockDisponible += cantidad;
        updateModificationTime();
    }
    
    @Override
    public String toString() {
        return "ProductoEntity{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", categoria='" + categoria + '\'' +
                ", activo=" + activo +
                ", stock=" + stockDisponible +
                ", version=" + version +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ProductoEntity that = (ProductoEntity) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}