package com.example.cafefidelidaqrdemo.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Cliente para configuración de Retrofit y servicios de API
 */
public class ApiClient {
    
    private static final String BASE_URL = "https://api.cafefidelidad.com/v1/";
    private static final int TIMEOUT_SECONDS = 30;
    
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    
    /**
     * Obtiene la instancia de Retrofit configurada
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Configurar logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Configurar OkHttpClient
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new AuthInterceptor());
            
            // Crear instancia de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * Obtiene la instancia del servicio API
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
    
    /**
     * Reinicia la instancia de Retrofit (útil para cambios de configuración)
     */
    public static void resetInstance() {
        retrofit = null;
        apiService = null;
    }
    
    /**
     * Configura una URL base personalizada
     */
    public static void setBaseUrl(String baseUrl) {
        resetInstance();
        // Aquí se podría implementar lógica para URL personalizada
    }
    
    /**
     * Verifica si el cliente está configurado
     */
    public static boolean isConfigured() {
        return retrofit != null && apiService != null;
    }
}