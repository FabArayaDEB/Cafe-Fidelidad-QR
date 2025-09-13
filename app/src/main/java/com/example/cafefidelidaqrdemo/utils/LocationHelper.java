package com.example.cafefidelidaqrdemo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;

// import com.google.android.gms.location.FusedLocationProviderClient;
// import com.google.android.gms.location.LocationCallback;
// import com.google.android.gms.location.LocationRequest;
// import com.google.android.gms.location.LocationResult;
// import com.google.android.gms.location.LocationServices;
// import com.google.android.gms.location.Priority;
// import com.google.android.gms.tasks.OnFailureListener;
// import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Helper para obtener ubicación del usuario usando Google Play Services
 */
public class LocationHelper {

    private final Context context;
    // private final FusedLocationProviderClient fusedLocationClient;
    // private LocationCallback locationCallback;
    private LocationUtils.LocationCallback callback;

    // Configuración de solicitud de ubicación
    // private static final long UPDATE_INTERVAL = 10000; // 10 segundos
    // private static final long FASTEST_INTERVAL = 5000; // 5 segundos
    // private static final long MAX_WAIT_TIME = 30000; // 30 segundos

    public LocationHelper(Context context) {
        this.context = context;
        // this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Obtiene la última ubicación conocida
     */
    /*
    @SuppressLint("MissingPermission")
    public void getLastKnownLocation(LocationUtils.LocationCallback callback) {
        if (!LocationUtils.hasLocationPermissions(context)) {
            callback.onPermissionDenied();
            return;
        }

        if (!LocationUtils.isLocationEnabled(context)) {
            callback.onLocationError("Los servicios de ubicación están deshabilitados");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            callback.onLocationReceived(location);
                        } else {
                            // Si no hay última ubicación, solicitar una nueva
                            requestCurrentLocation(callback);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onLocationError("Error al obtener ubicación: " + e.getMessage());
                    }
                });
    }
    */

    // Método simplificado sin Google Play Services
    public void getLastKnownLocation(LocationUtils.LocationCallback callback) {
        callback.onLocationError("Funcionalidad de ubicación temporalmente deshabilitada");
    }

    /**
     * Solicita la ubicación actual
     */
    /*
    @SuppressLint("MissingPermission")
    public void requestCurrentLocation(LocationUtils.LocationCallback callback) {
        if (!LocationUtils.hasLocationPermissions(context)) {
            callback.onPermissionDenied();
            return;
        }

        if (!LocationUtils.isLocationEnabled(context)) {
            callback.onLocationError("Los servicios de ubicación están deshabilitados");
            return;
        }

        this.callback = callback;

        // Crear solicitud de ubicación
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(MAX_WAIT_TIME)
                .build();

        // Crear callback para recibir actualizaciones
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Detener actualizaciones después de obtener la primera ubicación
                    stopLocationUpdates();
                    LocationHelper.this.callback.onLocationReceived(location);
                }
            }
        };

        // Solicitar actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onLocationError("Error al solicitar ubicación: " + e.getMessage());
            }
        });
    }
    */

    // Método simplificado sin Google Play Services
    public void requestCurrentLocation(LocationUtils.LocationCallback callback) {
        callback.onLocationError("Funcionalidad de ubicación temporalmente deshabilitada");
    }

    /**
     * Inicia actualizaciones continuas de ubicación
     */
    /*
    @SuppressLint("MissingPermission")
    public void startLocationUpdates(LocationUtils.LocationCallback callback) {
        if (!LocationUtils.hasLocationPermissions(context)) {
            callback.onPermissionDenied();
            return;
        }

        if (!LocationUtils.isLocationEnabled(context)) {
            callback.onLocationError("Los servicios de ubicación están deshabilitados");
            return;
        }

        this.callback = callback;

        // Crear solicitud de ubicación para actualizaciones continuas
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(MAX_WAIT_TIME)
                .build();

        // Crear callback para recibir actualizaciones
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LocationHelper.this.callback.onLocationReceived(location);
                }
            }
        };

        // Solicitar actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onLocationError("Error al iniciar actualizaciones: " + e.getMessage());
            }
        });
    }
    */

    // Método simplificado sin Google Play Services
    public void startLocationUpdates(LocationUtils.LocationCallback callback) {
        callback.onLocationError("Funcionalidad de ubicación temporalmente deshabilitada");
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    /*
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }
    */

    // Método simplificado sin Google Play Services
    public void stopLocationUpdates() {
        // No hay nada que detener sin Google Play Services
    }

    /**
     * Calcula distancias a múltiples puntos desde la ubicación actual
     */
    public void calculateDistancesToPoints(
            java.util.List<LocationUtils.GeoPoint> points,
            DistanceCalculationCallback callback) {
        
        getLastKnownLocation(new LocationUtils.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                java.util.List<DistanceResult> results = new java.util.ArrayList<>();
                
                for (int i = 0; i < points.size(); i++) {
                    LocationUtils.GeoPoint point = points.get(i);
                    double distance = LocationUtils.calculateDistance(
                            location.getLatitude(), location.getLongitude(),
                            point.getLatitude(), point.getLongitude()
                    );
                    
                    results.add(new DistanceResult(i, distance, 
                            LocationUtils.formatDistance(distance)));
                }
                
                callback.onDistancesCalculated(results);
            }

            @Override
            public void onLocationError(String error) {
                callback.onCalculationError(error);
            }

            @Override
            public void onPermissionDenied() {
                callback.onPermissionDenied();
            }
        });
    }

    /**
     * Libera recursos
     */
    public void cleanup() {
        stopLocationUpdates();
        callback = null;
    }

    /**
     * Interfaz para callbacks de cálculo de distancias
     */
    public interface DistanceCalculationCallback {
        void onDistancesCalculated(java.util.List<DistanceResult> results);
        void onCalculationError(String error);
        void onPermissionDenied();
    }

    /**
     * Clase para representar el resultado de un cálculo de distancia
     */
    public static class DistanceResult {
        private final int index;
        private final double distanceKm;
        private final String formattedDistance;

        public DistanceResult(int index, double distanceKm, String formattedDistance) {
            this.index = index;
            this.distanceKm = distanceKm;
            this.formattedDistance = formattedDistance;
        }

        public int getIndex() {
            return index;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public String getFormattedDistance() {
            return formattedDistance;
        }

        public int getDistanceIndicatorColor(Context context) {
            return LocationUtils.getDistanceIndicatorColor(context, distanceKm);
        }

        @Override
        public String toString() {
            return String.format("DistanceResult{index=%d, distance=%s}", 
                    index, formattedDistance);
        }
    }

    /**
     * Builder para configurar LocationHelper
     */
    /*
    public static class Builder {
        private final Context context;
        private long updateInterval = UPDATE_INTERVAL;
        private long fastestInterval = FASTEST_INTERVAL;
        private long maxWaitTime = MAX_WAIT_TIME;
        private int priority = Priority.PRIORITY_HIGH_ACCURACY;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setUpdateInterval(long updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public Builder setFastestInterval(long fastestInterval) {
            this.fastestInterval = fastestInterval;
            return this;
        }

            this.maxWaitTime = maxWaitTime;
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public LocationHelper build() {
            return new LocationHelper(context);
        }
    }
    */
}