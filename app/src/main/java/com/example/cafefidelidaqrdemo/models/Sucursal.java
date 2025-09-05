package com.example.cafefidelidaqrdemo.models;

import java.util.List;

public class Sucursal {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private double latitud;
    private double longitud;
    private String horarioApertura;
    private String horarioCierre;
    private boolean abierto;
    private String imagenUrl;
    private String descripcion;
    private List<String> servicios; // WiFi, Estacionamiento, Terraza, etc.
    private String gerente;
    private long fechaCreacion;
    private long fechaActualizacion;
    private boolean activa;
    private int capacidad;
    private String ciudad;
    private String codigoPostal;
    private double calificacion; // 1-5 estrellas
    private int numeroReseñas;
    private boolean tieneDelivery;
    private boolean tieneTakeaway;
    private String zonaDelivery; // Radio de entrega
    private double costoDelivery;

    // Constructor vacío requerido para Firebase
    public Sucursal() {}

    // Constructor básico
    public Sucursal(String id, String nombre, String direccion, String telefono, 
                   double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
        this.activa = true;
        this.abierto = true;
        this.calificacion = 0.0;
        this.numeroReseñas = 0;
        this.tieneDelivery = false;
        this.tieneTakeaway = true;
        this.costoDelivery = 0.0;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getHorarioApertura() {
        return horarioApertura;
    }

    public void setHorarioApertura(String horarioApertura) {
        this.horarioApertura = horarioApertura;
    }

    public String getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(String horarioCierre) {
        this.horarioCierre = horarioCierre;
    }

    public boolean isAbierto() {
        return abierto;
    }

    public void setAbierto(boolean abierto) {
        this.abierto = abierto;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getServicios() {
        return servicios;
    }

    public void setServicios(List<String> servicios) {
        this.servicios = servicios;
    }

    public String getGerente() {
        return gerente;
    }

    public void setGerente(String gerente) {
        this.gerente = gerente;
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

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public int getNumeroReseñas() {
        return numeroReseñas;
    }

    public void setNumeroReseñas(int numeroReseñas) {
        this.numeroReseñas = numeroReseñas;
    }

    public boolean isTieneDelivery() {
        return tieneDelivery;
    }

    public void setTieneDelivery(boolean tieneDelivery) {
        this.tieneDelivery = tieneDelivery;
    }

    public boolean isTieneTakeaway() {
        return tieneTakeaway;
    }

    public void setTieneTakeaway(boolean tieneTakeaway) {
        this.tieneTakeaway = tieneTakeaway;
    }

    public String getZonaDelivery() {
        return zonaDelivery;
    }

    public void setZonaDelivery(String zonaDelivery) {
        this.zonaDelivery = zonaDelivery;
    }

    public double getCostoDelivery() {
        return costoDelivery;
    }

    public void setCostoDelivery(double costoDelivery) {
        this.costoDelivery = costoDelivery;
    }

    // Métodos de utilidad
    public String getHorarioCompleto() {
        if (horarioApertura != null && horarioCierre != null) {
            return horarioApertura + " - " + horarioCierre;
        }
        return "Horario no disponible";
    }

    public String getDireccionCompleta() {
        StringBuilder direccionCompleta = new StringBuilder(direccion);
        if (ciudad != null && !ciudad.isEmpty()) {
            direccionCompleta.append(", ").append(ciudad);
        }
        if (codigoPostal != null && !codigoPostal.isEmpty()) {
            direccionCompleta.append(" ").append(codigoPostal);
        }
        return direccionCompleta.toString();
    }

    public boolean tieneServicio(String servicio) {
        return servicios != null && servicios.contains(servicio);
    }

    public String getCalificacionTexto() {
        if (numeroReseñas == 0) {
            return "Sin calificaciones";
        }
        return String.format("%.1f (%d reseñas)", calificacion, numeroReseñas);
    }
}