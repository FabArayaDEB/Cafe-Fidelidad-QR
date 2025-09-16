package com.example.cafefidelidaqrdemo.network;

import android.content.Context;

/**
 * Cliente API simplificado para el proyecto
 * Versión básica sin funcionalidades complejas de red
 */
public class ApiClient {
    private static ApiService apiService;
    
    public static ApiService getApiService() {
        if (apiService == null) {
            // Retornar instancia básica del RetrofitClient
            return RetrofitClient.getInstance(null).getApiService();
        }
        return apiService;
    }
    
    public static void initialize(Context context) {
        // Inicialización básica
        apiService = RetrofitClient.getInstance(context).getApiService();
    }
}