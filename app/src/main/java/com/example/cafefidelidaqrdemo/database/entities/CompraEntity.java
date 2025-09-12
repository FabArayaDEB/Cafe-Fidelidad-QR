package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Entidad que representa una compra realizada por un cliente.
 * Almacena información sobre la transacción, incluyendo el cliente,
 * monto, fecha y descripción.
 */
@Entity(tableName = "compras",
        foreignKeys = @ForeignKey(
                entity = ClienteEntity.class,
                parentColumns = "id_cliente",
                childColumns = "clienteId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("clienteId")})
public class CompraEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long clienteId;
    private double monto;
    private Date fecha;
    private String descripcion;
    private String mcId; // ID único del cliente para referencia rápida

    public CompraEntity(long clienteId, double monto, Date fecha, String descripcion, String mcId) {
        this.clienteId = clienteId;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.mcId = mcId;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public void setClienteId(long clienteId) {
        this.clienteId = clienteId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMcId() {
        return mcId;
    }

    public void setMcId(String mcId) {
        this.mcId = mcId;
    }
}