package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Versión simplificada de NetworkUtils para funciones básicas
 */
public class NetworkUtils {
    
    private static Context context;
    
    /**
     * Inicializa el contexto para NetworkUtils
     */
    public static void init(Context appContext) {
        context = appContext.getApplicationContext();
    }
    
    /**
     * Verifica si hay conexión a internet disponible
     */
    public static boolean isNetworkAvailable() {
        if (context == null) {
            return false;
        }
        return isNetworkAvailable(context);
    }
    
    /**
     * Verifica si hay conexión a internet disponible
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}