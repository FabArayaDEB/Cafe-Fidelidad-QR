package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ubicaciones")
public class UbicacionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "usuario_id")
    private int usuarioId;

    @ColumnInfo(name = "latitud")
    private double latitud;

    @ColumnInfo(name = "longitud")
    private double longitud;

    @ColumnInfo(name = "precision")
    private float precision;

    @ColumnInfo(name = "direccion")
    private String direccion;

    @ColumnInfo(name = "ciudad")
    private String ciudad;

    @ColumnInfo(name = "fecha_registro")
    private Date fechaRegistro;

    @ColumnInfo(name = "es_sucursal_cercana")
    private boolean esSucursalCercana;

    @ColumnInfo(name = "sucursal_id")
    private Integer sucursalId;

    @ColumnInfo(name = "distancia_sucursal")
    private Float distanciaSucursal;

    @ColumnInfo(name = "sincronizado")
    private boolean sincronizado;

    // Constructores
    public UbicacionEntity() {
        this.fechaRegistro = new Date();
        this.sincronizado = false;
    }

    public UbicacionEntity(int usuarioId, double latitud, double longitud, float precision) {
        this();
        this.usuarioId = usuarioId;
        this.latitud = latitud;
        this.longitud = longitud;
        this.precision = precision;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
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

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isEsSucursalCercana() {
        return esSucursalCercana;
    }

    public void setEsSucursalCercana(boolean esSucursalCercana) {
        this.esSucursalCercana = esSucursalCercana;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Float getDistanciaSucursal() {
        return distanciaSucursal;
    }

    public void setDistanciaSucursal(Float distanciaSucursal) {
        this.distanciaSucursal = distanciaSucursal;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }

    // Métodos de utilidad
    public String getCoordenadasString() {
        return String.format("%.6f, %.6f", latitud, longitud);
    }

    public boolean tieneUbicacionValida() {
        return latitud != 0.0 && longitud != 0.0;
    }

    @Override
    public String toString() {
        return "UbicacionEntity{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", precision=" + precision +
                ", direccion='" + direccion + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                ", esSucursalCercana=" + esSucursalCercana +
                ", sucursalId=" + sucursalId +
                ", distanciaSucursal=" + distanciaSucursal +
                ", sincronizado=" + sincronizado +
                '}';
    }
}