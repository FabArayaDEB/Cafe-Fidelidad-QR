package com.example.cafefidelidadqr;

public class cafeModel {
    String nombreCafe;
    String precioCafe;
    int imagen;



    public cafeModel(String nombreCafe, String precioCafe, int imagen) {
        this.nombreCafe = nombreCafe;
        this.precioCafe = precioCafe;
        this.imagen = imagen;

    }

    public String getNombreCafe() {
        return nombreCafe;
    }
    public String getPrecioCafe() {
        return precioCafe;
    }
    public int getImagen() {
        return imagen;
    }
}
