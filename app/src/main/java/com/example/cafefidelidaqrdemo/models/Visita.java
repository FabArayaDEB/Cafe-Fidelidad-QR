package com.example.cafefidelidaqrdemo.models;

public class Visita {
    private String id;
    private String userId;
    private String sucursal;
    private String direccionSucursal;
    private long fechaVisita;
    private double montoCompra;
    private int puntosGanados;
    private String qrCode;
    private String metodoPago;
    private String productos; // JSON string con los productos comprados

    public Visita() {
        // Constructor vacío requerido para Firebase
    }

    public Visita(String id, String userId, String sucursal, String direccionSucursal, 
                  long fechaVisita, double montoCompra, int puntosGanados, String qrCode, 
                  String metodoPago, String productos) {
        this.id = id;
        this.userId = userId;
        this.sucursal = sucursal;
        this.direccionSucursal = direccionSucursal;
        this.fechaVisita = fechaVisita;
        this.montoCompra = montoCompra;
        this.puntosGanados = puntosGanados;
        this.qrCode = qrCode;
        this.metodoPago = metodoPago;
        this.productos = productos;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSucursal() { return sucursal; }
    public void setSucursal(String sucursal) { this.sucursal = sucursal; }

    public String getDireccionSucursal() { return direccionSucursal; }
    public void setDireccionSucursal(String direccionSucursal) { this.direccionSucursal = direccionSucursal; }

    public long getFechaVisita() { return fechaVisita; }
    public void setFechaVisita(long fechaVisita) { this.fechaVisita = fechaVisita; }

    public double getMontoCompra() { return montoCompra; }
    public void setMontoCompra(double montoCompra) { this.montoCompra = montoCompra; }

    public int getPuntosGanados() { return puntosGanados; }
    public void setPuntosGanados(int puntosGanados) { this.puntosGanados = puntosGanados; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getProductos() { return productos; }
    public void setProductos(String productos) { this.productos = productos; }
}