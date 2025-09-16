package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Versión simplificada de SessionManager para funciones básicas
 */
public class SessionManager {
    private static final String PREF_NAME = "CafeFidelidadSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    
    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    
    /**
     * Crear sesión de usuario
     */
    public void createSession(String userId, String email, String name) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * Obtener ID del usuario
     */
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }
    
    /**
     * Obtener email del usuario
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * Obtener nombre del usuario
     */
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }
    
    /**
     * Verificar si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Cerrar sesión
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}