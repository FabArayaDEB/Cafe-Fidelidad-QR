package com.example.cafefidelidaqrdemo.models;

public class PromedioCalificacion {
    private double promedio;
    private int cantidad;

    public PromedioCalificacion() {}

    public PromedioCalificacion(double promedio, int cantidad) {
        this.promedio = promedio;
        this.cantidad = cantidad;
    }

    public double getPromedio() { return promedio; }
    public void setPromedio(double promedio) { this.promedio = promedio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}