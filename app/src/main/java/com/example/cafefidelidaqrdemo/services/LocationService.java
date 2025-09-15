package com.example.cafefidelidaqrdemo.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import com.example.cafefidelidaqrdemo.MainActivity;
import com.example.cafefidelidaqrdemo.R;
import com.example.cafefidelidaqrdemo.utils.LocationManager;

public class LocationService extends Service {
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private LocationManager locationManager;
    private boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        locationManager = new LocationManager(this);
        setupLocationObserver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isServiceRunning) {
            startForeground(NOTIFICATION_ID, createNotification());
            locationManager.startLocationUpdates();
            isServiceRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
        isServiceRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Canal para el servicio de ubicación");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Café Fidelidad")
                .setContentText("Detectando ubicación para ofertas cercanas")
                .setSmallIcon(R.drawable.ic_location) // Necesitarás crear este icono
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void setupLocationObserver() {
        locationManager.getCurrentLocation().observeForever(new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        });

        locationManager.getLocationError().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null) {
                    handleLocationError(error);
                }
            }
        });
    }

    private void handleLocationUpdate(Location location) {
        // Aquí puedes implementar lógica para:
        // - Verificar si hay sucursales cercanas
        // - Enviar notificaciones de ofertas locales
        // - Guardar ubicación en base de datos local
        // - Sincronizar con servidor
        
        // Ejemplo: Broadcast de nueva ubicación
        Intent locationIntent = new Intent("com.example.cafefidelidaqrdemo.LOCATION_UPDATE");
        locationIntent.putExtra("latitude", location.getLatitude());
        locationIntent.putExtra("longitude", location.getLongitude());
        locationIntent.putExtra("accuracy", location.getAccuracy());
        sendBroadcast(locationIntent);
    }

    private void handleLocationError(String error) {
        // Manejar errores de ubicación
        Intent errorIntent = new Intent("com.example.cafefidelidaqrdemo.LOCATION_ERROR");
        errorIntent.putExtra("error", error);
        sendBroadcast(errorIntent);
    }

    /**
     * Métodos estáticos para controlar el servicio
     */
    public static void startLocationService(android.content.Context context) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void stopLocationService(android.content.Context context) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.stopService(serviceIntent);
    }
}