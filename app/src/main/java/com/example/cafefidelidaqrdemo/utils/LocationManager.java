package com.example.cafefidelidaqrdemo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

/**
 * Versión simplificada de LocationManager para funciones básicas
 */
public class LocationManager implements LocationListener {
    
    private Context context;
    private android.location.LocationManager systemLocationManager;
    private MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private MutableLiveData<String> locationError = new MutableLiveData<>();
    
    public LocationManager(Context context) {
        this.context = context;
        this.systemLocationManager = (android.location.LocationManager) 
            context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    /**
     * Verifica si tiene permisos de ubicación
     */
    public boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Verifica si la ubicación está habilitada
     */
    public boolean isLocationEnabled() {
        if (systemLocationManager == null) {
            return false;
        }
        return systemLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
               systemLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }
    
    /**
     * Inicia las actualizaciones de ubicación
     */
    public void startLocationUpdates() {
        if (!hasLocationPermissions()) {
            locationError.setValue("Permisos de ubicación no concedidos");
            return;
        }
        
        if (!isLocationEnabled()) {
            locationError.setValue("Ubicación no habilitada");
            return;
        }
        
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
                systemLocationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER, 
                    5000, 10, this);
            }
        } catch (Exception e) {
            locationError.setValue("Error al iniciar ubicación: " + e.getMessage());
        }
    }
    
    /**
     * Detiene las actualizaciones de ubicación
     */
    public void stopLocationUpdates() {
        if (systemLocationManager != null) {
            systemLocationManager.removeUpdates(this);
        }
    }
    
    /**
     * Obtiene la última ubicación conocida
     */
    public Location getLastKnownLocation() {
        if (!hasLocationPermissions() || systemLocationManager == null) {
            return null;
        }
        
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
                return systemLocationManager.getLastKnownLocation(
                    android.location.LocationManager.GPS_PROVIDER);
            }
        } catch (Exception e) {
            locationError.setValue("Error al obtener ubicación: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Calcula la distancia entre dos ubicaciones
     */
    public double calculateDistance(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return -1;
        }
        return location1.distanceTo(location2);
    }
    
    /**
     * Verifica si está cerca de una ubicación
     */
    public boolean isNearLocation(Location targetLocation, double radiusInMeters) {
        Location current = getLastKnownLocation();
        if (current == null || targetLocation == null) {
            return false;
        }
        
        double distance = calculateDistance(current, targetLocation);
        return distance <= radiusInMeters;
    }
    
    /**
     * Obtiene LiveData de la ubicación actual
     */
    public MutableLiveData<Location> getCurrentLocation() {
        return currentLocation;
    }
    
    /**
     * Obtiene LiveData de errores de ubicación
     */
    public MutableLiveData<String> getLocationError() {
        return locationError;
    }
    
    /**
     * Limpia recursos
     */
    public void cleanup() {
        stopLocationUpdates();
    }
    
    // Implementación de LocationListener
    @Override
    public void onLocationChanged(Location location) {
        currentLocation.setValue(location);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // No implementado en versión simplificada
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        // No implementado en versión simplificada
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        locationError.setValue("Proveedor de ubicación deshabilitado: " + provider);
    }
}