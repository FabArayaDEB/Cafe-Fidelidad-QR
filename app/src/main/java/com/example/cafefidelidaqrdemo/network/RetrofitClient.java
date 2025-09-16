package com.example.cafefidelidaqrdemo.network;

import android.content.Context;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Retrofit simplificado para el proyecto
 * Versión básica sin funcionalidades complejas de red
 */
public class RetrofitClient {
    private static final String BASE_URL = "https://api.cafefidelidad.com/";
    private static RetrofitClient instance;
    private final ApiService apiService;
    
    private RetrofitClient(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(ApiService.class);
    }
    
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }
    
    public ApiService getApiService() {
        return apiService;
    }
}