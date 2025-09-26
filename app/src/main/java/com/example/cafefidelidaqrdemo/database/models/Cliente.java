package com.example.cafefidelidaqrdemo.database.models;

public class Cliente {
    private int id;
    private String nombre;
    private String email;
    private String telefono;
    private String password;
    private int puntosAcumulados;

    // Constructor vacío
    public Cliente() {
    }

    // Constructor completo
    public Cliente(int id, String nombre, String email, String telefono, String password, int puntosAcumulados) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.password = password;
        this.puntosAcumulados = puntosAcumulados;
    }

    // Constructor sin ID (para inserción)
    public Cliente(String nombre, String email, String telefono, String password, int puntosAcumulados) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.password = password;
        this.puntosAcumulados = puntosAcumulados;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public void setPuntosAcumulados(int puntosAcumulados) {
        this.puntosAcumulados = puntosAcumulados;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", puntosAcumulados=" + puntosAcumulados +
                '}';
    }
}