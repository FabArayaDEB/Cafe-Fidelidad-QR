package com.example.cafefidelidaqrdemo.network;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Cliente Retrofit singleton para manejo de conexiones de red
 */
public class RetrofitClient {
    
    private static final String BASE_URL = "https://api.cafefidelidad.com/";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private ApiService apiService;
    private Context context;
    
    private RetrofitClient(Context context) {
        this.context = context.getApplicationContext();
        setupRetrofit();
    }
    
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }
    
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RetrofitClient must be initialized with context first");
        }
        return instance;
    }
    
    private void setupRetrofit() {
        // Configurar logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Configurar cliente HTTP
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor);
        
        // Agregar interceptor de autenticación
        httpClient.addInterceptor(new AuthInterceptor());
        
        // Construir Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // Crear servicio API
        apiService = retrofit.create(ApiService.class);
    }
    
    public ApiService getApiService() {
        return apiService;
    }
    
    public Retrofit getRetrofit() {
        return retrofit;
    }
    
    /**
     * Actualiza el token de autenticación
     */
    public void updateAuthToken(String token) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("auth_token", token).apply();
            
            // Recrear retrofit con nuevo token
            setupRetrofit();
        }
    }
    
    /**
     * Limpia el token de autenticación
     */
    public void clearAuthToken() {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
            prefs.edit().remove("auth_token").apply();
            
            // Recrear retrofit sin token
            setupRetrofit();
        }
    }
    
    /**
     * Verifica si hay conexión de red disponible
     */
    public boolean isNetworkAvailable() {
        if (context == null) return false;
        
        android.net.ConnectivityManager connectivityManager = 
            (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
    }
}