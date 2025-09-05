package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Gestor de sesión para manejar datos del usuario autenticado
 */
public class SessionManager {
    private static final String PREF_NAME = "CafeFidelidadSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private FirebaseAuth firebaseAuth;
    
    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        firebaseAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    public String getUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return preferences.getString(KEY_USER_ID, null);
    }
    
    /**
     * Obtiene el email del usuario actual
     */
    public String getUserEmail() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return preferences.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * Obtiene el nombre del usuario actual
     */
    public String getUserName() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getDisplayName();
        }
        return preferences.getString(KEY_USER_NAME, null);
    }
    
    /**
     * Verifica si el usuario está logueado
     */
    public boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null || preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Guarda los datos de sesión del usuario
     */
    public void createSession(String userId, String email, String name) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * Limpia la sesión del usuario
     */
    public void logout() {
        editor.clear();
        editor.apply();
        firebaseAuth.signOut();
    }
    
    /**
     * Actualiza los datos del usuario en la sesión
     */
    public void updateUserData(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
}