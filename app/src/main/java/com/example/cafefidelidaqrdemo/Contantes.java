package com.example.cafefidelidaqrdemo;

import java.util.Calendar;

public class Contantes {

    // Constantes para el programa de fidelidad
    public static final int PUNTOS_POR_PESO = 1; // 1 punto por cada peso gastado
    public static final int PUNTOS_BIENVENIDA = 100;
    // Sistema de niveles eliminado

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
    
    // Sistema de niveles eliminado: no se calcula nivel
    
    // Calcular puntos por compra
    public static int calcularPuntos(double montoCompra) {
        return (int) Math.floor(montoCompra * PUNTOS_POR_PESO);
    }
    
    // Sistema de niveles eliminado: no hay siguiente nivel
    
    // Formatear fecha para mostrar en el historial
    public static String formatearFecha(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
    }
}