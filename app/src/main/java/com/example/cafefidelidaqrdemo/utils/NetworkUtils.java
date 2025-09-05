package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * Utilidades para verificación de conectividad de red
 */
public class NetworkUtils {
    
    private static Context applicationContext;
    
    /**
     * Inicializa NetworkUtils con el contexto de la aplicación
     */
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }
    
    /**
     * Verifica si hay conexión a internet disponible
     */
    public static boolean isNetworkAvailable() {
        if (applicationContext == null) {
            return false;
        }
        return isNetworkAvailable(applicationContext);
    }
    
    /**
     * Verifica si hay conexión a internet disponible
     * @param context Contexto de la aplicación
     * @return true si hay conexión, false en caso contrario
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Para API 23 y superior
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && 
                   (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // Para API menor a 23
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
    
    /**
     * Verifica si hay conexión WiFi
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
    }
    
    /**
     * Verifica si hay conexión de datos móviles
     */
    public static boolean isMobileConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } else {
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileInfo != null && mobileInfo.isConnected();
        }
    }
    
    /**
     * Obtiene el tipo de conexión actual
     */
    public static String getConnectionType(Context context) {
        if (!isNetworkAvailable(context)) {
            return "Sin conexión";
        }
        
        if (isWifiConnected(context)) {
            return "WiFi";
        } else if (isMobileConnected(context)) {
            return "Datos móviles";
        } else {
            return "Desconocido";
        }
    }
}