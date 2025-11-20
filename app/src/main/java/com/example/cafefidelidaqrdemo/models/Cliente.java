package com.example.cafefidelidaqrdemo.models;

/**
 * Modelo simplificado para Cliente
 */
public class Cliente {
    private String id;
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String estado;
    private long fechaCreacion;
    private long fechaActualizacion;
    private int totalVisitas;
    private int puntosAcumulados;
    private boolean activo;

    // Constructor vacío
    public Cliente() {}

    // Constructor principal
    public Cliente(String id, String nombre, String email, String telefono, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
        this.activo = true;
        this.totalVisitas = 0;
        this.puntosAcumulados = 0;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public int getTotalVisitas() { return totalVisitas; }
    public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }

    public int getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(int puntosAcumulados) { this.puntosAcumulados = puntosAcumulados; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // Métodos de utilidad
    public void incrementarVisitas() {
        this.totalVisitas++;
        this.fechaActualizacion = System.currentTimeMillis();
    }

    public void agregarPuntos(int puntos) {
        this.puntosAcumulados += puntos;
        this.fechaActualizacion = System.currentTimeMillis();
    }

    public void restarPuntos(int puntos) {
        this.puntosAcumulados = Math.max(0, this.puntosAcumulados - puntos);
        this.fechaActualizacion = System.currentTimeMillis();
    }

    // Sistema de niveles eliminado: no se expone nivel

    @Override
    public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", totalVisitas=" + totalVisitas +
                ", puntosAcumulados=" + puntosAcumulados +
                ", activo=" + activo +
                '}';
    }
}