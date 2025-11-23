package com.example.cafefidelidaqrdemo.models;

public class CodigoQr {
    private Integer IdCodigoQr;
    private String idCliente;
    private String ContentQr;
    private boolean isScanned;
    private long generationTime;
    private String Estado;

    public CodigoQr() {}

    // Constructor actualizado
    public CodigoQr(String idCliente, String ContentQr, long generationTime) {
        this.idCliente = idCliente;
        this.ContentQr = ContentQr;
        this.generationTime = generationTime;
    }

    // Constructor completo actualizado
    public CodigoQr(Integer idCodigoQr, String idCliente, String contentQr, boolean isScanned, long generationTime, String Estado) {
        this.IdCodigoQr = idCodigoQr;
        this.idCliente = idCliente;
        this.ContentQr = contentQr;
        this.isScanned = isScanned;
        this.generationTime = generationTime;
        this.Estado = Estado;
    }

    public Integer getIdCodigoQr() { return IdCodigoQr; }
    public void setIdCodigoQr(Integer idCodigoQr) { this.IdCodigoQr = idCodigoQr; }

    // Getters y Setters actualizados a String
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getContentQr() { return ContentQr; }
    public void setContentQr(String contentQr) { this.ContentQr = contentQr; }

    public boolean isScanned() { return isScanned; }
    public void setIsScanned(boolean isScanned) { this.isScanned = isScanned; }

    public long getGenerationTime() { return generationTime; }
    public void setGenerationTime(long generationTime) { this.generationTime = generationTime; }

    public String getEstado() { return Estado; }
    public void setEstado(String Estado) { this.Estado = Estado; }
}