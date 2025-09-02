package com.example.cafefidelidaqrdemo;

import java.util.Calendar;

public class Contantes {

    // Constantes para el programa de fidelidad
    public static final int PUNTOS_POR_PESO = 1; // 1 punto por cada peso gastado
    public static final int PUNTOS_BIENVENIDA = 100;
    public static final int PUNTOS_BRONCE = 0;
    public static final int PUNTOS_PLATA = 500;
    public static final int PUNTOS_ORO = 1000;
    public static final int PUNTOS_PLATINO = 2000;
    
    // Niveles de fidelidad
    public static final String NIVEL_BRONCE = "Bronce";
    public static final String NIVEL_PLATA = "Plata";
    public static final String NIVEL_ORO = "Oro";
    public static final String NIVEL_PLATINO = "Platino";

    public static long getTimeD(){
        return System.currentTimeMillis();
    }

    public static String DateFormat(Long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        String date = android.text.format.DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }
    
    public static String DateTimeFormat(Long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        String dateTime = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
        return dateTime;
    }
    
    // Calcular nivel basado en puntos
    public static String calcularNivel(int puntos) {
        if (puntos >= PUNTOS_PLATINO) {
            return NIVEL_PLATINO;
        } else if (puntos >= PUNTOS_ORO) {
            return NIVEL_ORO;
        } else if (puntos >= PUNTOS_PLATA) {
            return NIVEL_PLATA;
        } else {
            return NIVEL_BRONCE;
        }
    }
    
    // Calcular puntos por compra
    public static int calcularPuntos(double montoCompra) {
        return (int) Math.floor(montoCompra * PUNTOS_POR_PESO);
    }
    
    // Obtener puntos necesarios para el siguiente nivel
    public static int puntosParaSiguienteNivel(int puntosActuales) {
        if (puntosActuales < PUNTOS_PLATA) {
            return PUNTOS_PLATA - puntosActuales;
        } else if (puntosActuales < PUNTOS_ORO) {
            return PUNTOS_ORO - puntosActuales;
        } else if (puntosActuales < PUNTOS_PLATINO) {
            return PUNTOS_PLATINO - puntosActuales;
        } else {
            return 0; // Ya está en el nivel máximo
        }
    }
}