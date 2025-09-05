package com.example.cafefidelidaqrdemo.models;

/**
 * Modelo para Cliente
 */
public class Cliente {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String fechaNacimiento;
    private String estado;
    private long fechaCreacion;
    private long fechaActualizacion;
    private int totalVisitas;
    private int puntosAcumulados;
    private String sucursalFavorita;
    private boolean activo;
    private String tipoCliente; // "regular", "premium", "vip"
    private String genero;
    private String ciudad;
    private String codigoPostal;
    private String fechaUltimaVisita;
    private double montoTotalCompras;
    private String preferenciasNotificacion; // JSON string
    private boolean aceptaMarketing;
    private String referidoPor;
    private String codigoReferido;
    
    // Constructor vacío
    public Cliente() {}
    
    // Constructor completo
    public Cliente(String id, String nombre, String email, String telefono, 
                 String fechaNacimiento, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.estado = estado;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
        this.activo = true;
        this.tipoCliente = "regular";
        this.totalVisitas = 0;
        this.puntosAcumulados = 0;
        this.montoTotalCompras = 0.0;
        this.aceptaMarketing = true;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
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
    
    public String getSucursalFavorita() { return sucursalFavorita; }
    public void setSucursalFavorita(String sucursalFavorita) { this.sucursalFavorita = sucursalFavorita; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    
    public String getFechaUltimaVisita() { return fechaUltimaVisita; }
    public void setFechaUltimaVisita(String fechaUltimaVisita) { this.fechaUltimaVisita = fechaUltimaVisita; }
    
    public double getMontoTotalCompras() { return montoTotalCompras; }
    public void setMontoTotalCompras(double montoTotalCompras) { this.montoTotalCompras = montoTotalCompras; }
    
    public String getPreferenciasNotificacion() { return preferenciasNotificacion; }
    public void setPreferenciasNotificacion(String preferenciasNotificacion) { this.preferenciasNotificacion = preferenciasNotificacion; }
    
    public boolean isAceptaMarketing() { return aceptaMarketing; }
    public void setAceptaMarketing(boolean aceptaMarketing) { this.aceptaMarketing = aceptaMarketing; }
    
    public String getReferidoPor() { return referidoPor; }
    public void setReferidoPor(String referidoPor) { this.referidoPor = referidoPor; }
    
    public String getCodigoReferido() { return codigoReferido; }
    public void setCodigoReferido(String codigoReferido) { this.codigoReferido = codigoReferido; }
    
    // Métodos de utilidad
    public boolean esPremium() {
        return "premium".equals(tipoCliente) || "vip".equals(tipoCliente);
    }
    
    public boolean esVip() {
        return "vip".equals(tipoCliente);
    }
    
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
    
    public void agregarCompra(double monto) {
        this.montoTotalCompras += monto;
        this.fechaActualizacion = System.currentTimeMillis();
    }
    
    public double getPromedioCompra() {
        return totalVisitas > 0 ? montoTotalCompras / totalVisitas : 0.0;
    }
    
    @Override
    public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", estado='" + estado + '\'' +
                ", tipoCliente='" + tipoCliente + '\'' +
                ", totalVisitas=" + totalVisitas +
                ", puntosAcumulados=" + puntosAcumulados +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return id != null ? id.equals(cliente.id) : cliente.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}