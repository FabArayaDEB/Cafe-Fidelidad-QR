package com.example.cafefidelidaqrdemo.database.models;

public class Beneficio {
    private int id;
    private String nombre;
    private String descripcion;
    private int puntosRequeridos;
    private String tipo;
    private boolean activo;
    private long vigencia_fin;
    private String regla;

    // Constructor vacío
    public Beneficio() {
    }

    // Constructor completo
    public Beneficio(int id, String nombre, String descripcion, int puntosRequeridos, String tipo, boolean activo, long vigencia_fin, String regla) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosRequeridos = puntosRequeridos;
        this.tipo = tipo;
        this.activo = activo;
        this.vigencia_fin = vigencia_fin;
        this.regla = regla;
    }

    // Constructor sin ID (para inserción)
    public Beneficio(String nombre, String descripcion, int puntosRequeridos, String tipo, boolean activo, long vigencia_fin, String regla) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosRequeridos = puntosRequeridos;
        this.tipo = tipo;
        this.activo = activo;
        this.vigencia_fin = vigencia_fin;
        this.regla = regla;
    }

    // Constructor básico (para compatibilidad)
    public Beneficio(int id, String nombre, String descripcion, int puntosRequeridos, String tipo, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosRequeridos = puntosRequeridos;
        this.tipo = tipo;
        this.activo = activo;
        this.vigencia_fin = 0;
        this.regla = "";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getPuntosRequeridos() {
        return puntosRequeridos;
    }

    public void setPuntosRequeridos(int puntosRequeridos) {
        this.puntosRequeridos = puntosRequeridos;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public long getVigencia_fin() {
        return vigencia_fin;
    }

    public void setVigencia_fin(long vigencia_fin) {
        this.vigencia_fin = vigencia_fin;
    }

    public String getRegla() {
        return regla;
    }

    public void setRegla(String regla) {
        this.regla = regla;
    }

    @Override
    public String toString() {
        return "Beneficio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", puntosRequeridos=" + puntosRequeridos +
                ", tipo='" + tipo + '\'' +
                ", activo=" + activo +
                ", vigencia_fin=" + vigencia_fin +
                ", regla='" + regla + '\'' +
                '}';
    }
}