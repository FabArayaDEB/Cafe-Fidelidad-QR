package com.example.cafefidelidaqrdemo.models;

public class ResenaProducto {
    private int id;
    private int productoId;
    private int usuarioId;
    private int calificacion; // 1..5
    private String comentario; // opcional
    private long fechaCreacion; // epoch millis
    private long fechaActualizacion; // epoch millis

    public ResenaProducto() {}

    public ResenaProducto(int id, int productoId, int usuarioId, int calificacion, String comentario, long fechaCreacion, long fechaActualizacion) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}