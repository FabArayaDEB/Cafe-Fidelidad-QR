package com.example.cafefidelidaqrdemo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;

/**
 * Utilidades de ubicación simplificadas
 * Proporciona funciones básicas para manejo de ubicación
 */
public class LocationUtils {
    
    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Calcula la distancia entre dos puntos en kilómetros
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Calcula la distancia entre dos objetos Location
     */
    public static double calculateDistance(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return Double.MAX_VALUE;
        }
        
        return calculateDistance(
            location1.getLatitude(), location1.getLongitude(),
            location2.getLatitude(), location2.getLongitude()
        );
    }
    
    /**
     * Verifica si una ubicación está dentro de un radio específico
     */
    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }
    
    /**
     * Formatea la distancia para mostrar al usuario
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }
    
    /**
     * Valida si las coordenadas son válidas
     */
    public static boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
    
    /**
     * Crea un objeto Location a partir de coordenadas
     */
    public static Location createLocation(double latitude, double longitude) {
        if (!isValidCoordinates(latitude, longitude)) {
            return null;
        }
        
        Location location = new Location("manual");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}