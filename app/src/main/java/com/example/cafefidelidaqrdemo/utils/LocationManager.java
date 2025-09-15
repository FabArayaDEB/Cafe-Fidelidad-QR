package com.example.cafefidelidaqrdemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LocationManager implements LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private Context context;
    private android.location.LocationManager locationManager;
    private MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private MutableLiveData<Boolean> locationPermissionGranted = new MutableLiveData<>();
    private MutableLiveData<String> locationError = new MutableLiveData<>();

    public LocationManager(Context context) {
        this.context = context;
        this.locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermissions();
    }

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    public boolean hasLocationPermissions() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica si la ubicación está habilitada en el dispositivo
     */
    public boolean isLocationEnabled() {
        return locationManager != null && 
               (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER));
    }

    /**
     * Solicita permisos de ubicación
     */
    public void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Verifica y actualiza el estado de los permisos
     */
    private void checkLocationPermissions() {
        boolean hasPermissions = hasLocationPermissions();
        locationPermissionGranted.setValue(hasPermissions);
    }

    /**
     * Inicia la obtención de ubicación
     */
    public void startLocationUpdates() {
        if (!hasLocationPermissions()) {
            locationError.setValue("Permisos de ubicación no concedidos");
            return;
        }

        if (locationManager == null) {
            locationError.setValue("Servicio de ubicación no disponible");
            return;
        }

        try {
            // Verificar si GPS está habilitado
            if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                locationError.setValue("GPS y red deshabilitados. Habilite al menos uno para obtener ubicación.");
                return;
            }

            // Solicitar actualizaciones de ubicación
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        5000, // 5 segundos
                        10,    // 10 metros
                        this
                );
            }

            if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        5000, // 5 segundos
                        10,    // 10 metros
                        this
                );
            }

            // Obtener última ubicación conocida
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                currentLocation.setValue(lastKnownLocation);
            }

        } catch (SecurityException e) {
            locationError.setValue("Error de seguridad al acceder a la ubicación: " + e.getMessage());
        }
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    public void stopLocationUpdates() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                locationError.setValue("Error al detener actualizaciones de ubicación: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene la última ubicación conocida
     */
    public Location getLastKnownLocation() {
        if (!hasLocationPermissions()) {
            return null;
        }

        try {
            Location gpsLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);

            // Retornar la ubicación más reciente
            if (gpsLocation != null && networkLocation != null) {
                return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            } else if (gpsLocation != null) {
                return gpsLocation;
            } else {
                return networkLocation;
            }
        } catch (SecurityException e) {
            locationError.setValue("Error al obtener última ubicación: " + e.getMessage());
            return null;
        }
    }

    /**
     * Calcula la distancia entre dos ubicaciones en metros
     */
    public static float calculateDistance(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return -1;
        }
        return location1.distanceTo(location2);
    }

    /**
     * Verifica si el usuario está cerca de una ubicación específica
     */
    public boolean isNearLocation(Location targetLocation, float radiusInMeters) {
        Location currentLoc = currentLocation.getValue();
        if (currentLoc == null || targetLocation == null) {
            return false;
        }
        return calculateDistance(currentLoc, targetLocation) <= radiusInMeters;
    }

    // LiveData getters
    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<Boolean> getLocationPermissionGranted() {
        return locationPermissionGranted;
    }

    public LiveData<String> getLocationError() {
        return locationError;
    }

    // LocationListener implementation
    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation.setValue(location);
        locationError.setValue(null); // Limpiar errores previos
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Provider habilitado
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        locationError.setValue("Proveedor de ubicación " + provider + " deshabilitado");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Estado del proveedor cambió
    }

    /**
     * Maneja el resultado de la solicitud de permisos
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            locationPermissionGranted.setValue(allPermissionsGranted);
            
            if (allPermissionsGranted) {
                startLocationUpdates();
            } else {
                locationError.setValue("Permisos de ubicación denegados");
            }
        }
    }

    /**
     * Limpia recursos
     */
    public void cleanup() {
        stopLocationUpdates();
    }
}