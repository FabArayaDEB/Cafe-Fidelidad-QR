package com.example.cafefidelidaqrdemo.models;

public class Canje {
    private String id;
    private String userId;
    private String tipo; // "descuento_porcentaje", "monto_fijo", "2x1", "producto_gratis"
    private String descripcion;
    private double valor; // porcentaje de descuento o monto fijo
    private String productoGratis; // nombre del producto gratis (si aplica)
    private long fechaCanje;
    private String sucursal;
    private String codigoVerificacion;
    private boolean usado;
    private long fechaExpiracion;
    
    // Campos adicionales para compatibilidad con base de datos
    private int clienteId;
    private int beneficioId;
    private int puntosUtilizados;
    private String estado;

    public Canje() {
        // Constructor vacío requerido para Firebase
    }

    public Canje(String id, String userId, String tipo, String descripcion, double valor, 
                 String productoGratis, long fechaCanje, String sucursal, String codigoVerificacion, 
                 boolean usado, long fechaExpiracion) {
        this.id = id;
        this.userId = userId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.valor = valor;
        this.productoGratis = productoGratis;
        this.fechaCanje = fechaCanje;
        this.sucursal = sucursal;
        this.codigoVerificacion = codigoVerificacion;
        this.usado = usado;
        this.fechaExpiracion = fechaExpiracion;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getProductoGratis() { return productoGratis; }
    public void setProductoGratis(String productoGratis) { this.productoGratis = productoGratis; }

    public long getFechaCanje() { return fechaCanje; }
    public void setFechaCanje(long fechaCanje) { this.fechaCanje = fechaCanje; }

    public String getSucursal() { return sucursal; }
    public void setSucursal(String sucursal) { this.sucursal = sucursal; }

    public String getCodigoVerificacion() { return codigoVerificacion; }
    public void setCodigoVerificacion(String codigoVerificacion) { this.codigoVerificacion = codigoVerificacion; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }

    public long getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(long fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    
    // Getters y Setters para campos adicionales de base de datos
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    
    public int getBeneficioId() { return beneficioId; }
    public void setBeneficioId(int beneficioId) { this.beneficioId = beneficioId; }
    
    public int getPuntosUtilizados() { return puntosUtilizados; }
    public void setPuntosUtilizados(int puntosUtilizados) { this.puntosUtilizados = puntosUtilizados; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    // Método de compatibilidad para getPuntosUsados (usado en HistorialAdapter)
    public int getPuntosUsados() { return puntosUtilizados; }
}