package com.example.cafefidelidaqrdemo.models;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String categoria;
    private String imagenUrl;
    private boolean disponible;
    private String sucursalId;
    private int stock;
    private long fechaCreacion;
    private long fechaActualizacion;
    private String ingredientes;
    private int calorias;
    private boolean esVegano;
    private boolean esVegetariano;
    private boolean contieneLactosa;
    private boolean contieneGluten;
    private double descuento; // Porcentaje de descuento (0-100)
    private boolean esPopular;
    private int puntosRequeridos; // Para canjes con puntos

    // Constructor vacío requerido para Firebase
    public Producto() {}

    // Constructor completo
    public Producto(String id, String nombre, String descripcion, double precio, 
                   String categoria, String imagenUrl, boolean disponible, 
                   String sucursalId, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
        this.disponible = disponible;
        this.sucursalId = sucursalId;
        this.stock = stock;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
        this.descuento = 0.0;
        this.esPopular = false;
        this.puntosRequeridos = 0;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(long fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public int getCalorias() {
        return calorias;
    }

    public void setCalorias(int calorias) {
        this.calorias = calorias;
    }

    public boolean isEsVegano() {
        return esVegano;
    }

    public void setEsVegano(boolean esVegano) {
        this.esVegano = esVegano;
    }

    public boolean isEsVegetariano() {
        return esVegetariano;
    }

    public void setEsVegetariano(boolean esVegetariano) {
        this.esVegetariano = esVegetariano;
    }

    public boolean isContieneLactosa() {
        return contieneLactosa;
    }

    public void setContieneLactosa(boolean contieneLactosa) {
        this.contieneLactosa = contieneLactosa;
    }

    public boolean isContieneGluten() {
        return contieneGluten;
    }

    public void setContieneGluten(boolean contieneGluten) {
        this.contieneGluten = contieneGluten;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public boolean isEsPopular() {
        return esPopular;
    }

    public void setEsPopular(boolean esPopular) {
        this.esPopular = esPopular;
    }

    public int getPuntosRequeridos() {
        return puntosRequeridos;
    }

    public void setPuntosRequeridos(int puntosRequeridos) {
        this.puntosRequeridos = puntosRequeridos;
    }

    // Métodos de utilidad
    public double getPrecioConDescuento() {
        if (descuento > 0) {
            return precio * (1 - descuento / 100);
        }
        return precio;
    }

    public boolean tieneDescuento() {
        return descuento > 0;
    }

    public boolean estaEnStock() {
        return stock > 0;
    }

    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}