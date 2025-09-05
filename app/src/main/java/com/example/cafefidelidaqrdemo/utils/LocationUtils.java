package com.example.cafefidelidaqrdemo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Utilidades para manejo de geolocalización y cálculo de distancias
 */
public class LocationUtils {

    // Constantes para permisos
    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Radio de la Tierra en kilómetros
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    public static boolean hasLocationPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica si el permiso de ubicación precisa está concedido
     */
    public static boolean hasFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicita permisos de ubicación
     */
    public static void requestLocationPermissions(Fragment fragment) {
        fragment.requestPermissions(LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Verifica si el GPS está habilitado
     */
    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && 
               locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Verifica si la red de ubicación está habilitada
     */
    public static boolean isNetworkLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Verifica si algún proveedor de ubicación está disponible
     */
    public static boolean isLocationEnabled(Context context) {
        return isGpsEnabled(context) || isNetworkLocationEnabled(context);
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * 
     * @param lat1 Latitud del primer punto
     * @param lon1 Longitud del primer punto
     * @param lat2 Latitud del segundo punto
     * @param lon2 Longitud del segundo punto
     * @return Distancia en kilómetros
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convertir grados a radianes
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Diferencias
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Fórmula de Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calcula la distancia usando objetos Location
     */
    public static double calculateDistance(Location location1, Location location2) {
        return calculateDistance(
            location1.getLatitude(), location1.getLongitude(),
            location2.getLatitude(), location2.getLongitude()
        );
    }

    /**
     * Formatea la distancia para mostrar en UI
     * 
     * @param distanceKm Distancia en kilómetros
     * @return String formateado (ej: "1.2 km" o "850 m")
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // Mostrar en metros si es menos de 1 km
            int meters = (int) Math.round(distanceKm * 1000);
            return meters + " m";
        } else {
            // Mostrar en kilómetros con 1 decimal
            return String.format("%.1f km", distanceKm);
        }
    }

    /**
     * Obtiene el color para el indicador de distancia basado en la proximidad
     * 
     * @param context Contexto para obtener colores
     * @param distanceKm Distancia en kilómetros
     * @return ID del color a usar
     */
    public static int getDistanceIndicatorColor(Context context, double distanceKm) {
        if (distanceKm <= 0.5) {
            // Muy cerca - Verde
            return ContextCompat.getColor(context, android.R.color.holo_green_dark);
        } else if (distanceKm <= 2.0) {
            // Cerca - Amarillo
            return ContextCompat.getColor(context, android.R.color.holo_orange_light);
        } else if (distanceKm <= 5.0) {
            // Moderado - Naranja
            return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
        } else {
            // Lejos - Rojo
            return ContextCompat.getColor(context, android.R.color.holo_red_dark);
        }
    }

    /**
     * Crea un objeto Location a partir de coordenadas
     */
    public static Location createLocation(double latitude, double longitude) {
        Location location = new Location("manual");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    /**
     * Valida si las coordenadas son válidas
     */
    public static boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0 && 
               longitude >= -180.0 && longitude <= 180.0 &&
               !(latitude == 0.0 && longitude == 0.0);
    }

    /**
     * Interfaz para callbacks de ubicación
     */
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
        void onPermissionDenied();
    }

    /**
     * Interfaz para callbacks de distancia
     */
    public interface DistanceCallback {
        void onDistanceCalculated(double distanceKm, String formattedDistance);
        void onDistanceError(String error);
    }

    /**
     * Clase para representar un punto geográfico
     */
    public static class GeoPoint {
        private final double latitude;
        private final double longitude;

        public GeoPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double distanceTo(GeoPoint other) {
            return LocationUtils.calculateDistance(
                this.latitude, this.longitude,
                other.latitude, other.longitude
            );
        }

        public Location toLocation() {
            return LocationUtils.createLocation(latitude, longitude);
        }

        @Override
        public String toString() {
            return String.format("GeoPoint(%.6f, %.6f)", latitude, longitude);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GeoPoint geoPoint = (GeoPoint) obj;
            return Double.compare(geoPoint.latitude, latitude) == 0 &&
                   Double.compare(geoPoint.longitude, longitude) == 0;
        }

        @Override
        public int hashCode() {
            long latBits = Double.doubleToLongBits(latitude);
            long lonBits = Double.doubleToLongBits(longitude);
            return (int) (latBits ^ (latBits >>> 32) ^ lonBits ^ (lonBits >>> 32));
        }
    }
}