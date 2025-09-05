package com.example.cafefidelidaqrdemo.network;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * Interceptor para agregar headers de autenticación a las peticiones HTTP
 */
public class AuthInterceptor implements Interceptor {
    
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_API_KEY = "api_key";
    
    private static Context applicationContext;
    private static String staticToken;
    private static String staticApiKey;
    
    /**
     * Inicializa el interceptor con el contexto de la aplicación
     */
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }
    
    /**
     * Establece un token estático para todas las peticiones
     */
    public static void setStaticToken(String token) {
        staticToken = token;
    }
    
    /**
     * Establece una API key estática para todas las peticiones
     */
    public static void setStaticApiKey(String apiKey) {
        staticApiKey = apiKey;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();
        
        // Agregar headers comunes
        requestBuilder.addHeader("Content-Type", "application/json");
        requestBuilder.addHeader("Accept", "application/json");
        
        // Agregar token de autenticación
        String token = getAuthToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        
        // Agregar API key si está disponible
        String apiKey = getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            requestBuilder.addHeader("X-API-Key", apiKey);
        }
        
        // Agregar headers adicionales para identificación de la app
        requestBuilder.addHeader("User-Agent", "CafeFidelidadApp/1.0");
        requestBuilder.addHeader("X-App-Version", "1.0.0");
        
        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }
    
    /**
     * Obtiene el token de autenticación desde SharedPreferences o valor estático
     */
    private String getAuthToken() {
        // Priorizar token estático
        if (staticToken != null && !staticToken.isEmpty()) {
            return staticToken;
        }
        
        // Obtener desde SharedPreferences
        if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_TOKEN, null);
        }
        
        return null;
    }
    
    /**
     * Obtiene la API key desde SharedPreferences o valor estático
     */
    private String getApiKey() {
        // Priorizar API key estática
        if (staticApiKey != null && !staticApiKey.isEmpty()) {
            return staticApiKey;
        }
        
        // Obtener desde SharedPreferences
        if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_API_KEY, null);
        }
        
        return null;
    }
    
    /**
     * Guarda el token de autenticación en SharedPreferences
     */
    public static void saveAuthToken(String token) {
        if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_TOKEN, token).apply();
        }
    }
    
    /**
     * Guarda la API key en SharedPreferences
     */
    public static void saveApiKey(String apiKey) {
        if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_API_KEY, apiKey).apply();
        }
    }
    
    /**
     * Limpia los datos de autenticación
     */
    public static void clearAuth() {
        staticToken = null;
        staticApiKey = null;
        
        if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }
    
    /**
     * Verifica si hay token de autenticación disponible
     */
    public static boolean hasAuthToken() {
        String token = null;
        
        if (staticToken != null && !staticToken.isEmpty()) {
            token = staticToken;
        } else if (applicationContext != null) {
            SharedPreferences prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            token = prefs.getString(KEY_TOKEN, null);
        }
        
        return token != null && !token.isEmpty();
    }
}