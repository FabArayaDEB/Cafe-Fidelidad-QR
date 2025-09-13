package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

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
    // private FirebaseAuth firebaseAuth;
    
    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        // firebaseAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    public String getUserId() {
        // Priorizar datos locales para usuarios locales
        String localUserId = preferences.getString(KEY_USER_ID, null);
        if (localUserId != null) {
            return localUserId;
        }
        
        // Fallback a Firebase si no hay datos locales
        /*
        // FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        */
        return null;
    }
    
    /**
     * Obtiene el email del usuario actual
     */
    public String getUserEmail() {
        // Priorizar datos locales para usuarios locales
        String localEmail = preferences.getString(KEY_USER_EMAIL, null);
        if (localEmail != null) {
            return localEmail;
        }
        
        // Fallback a Firebase si no hay datos locales
        /*
        // FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        */
        return null;
    }
    
    /**
     * Obtiene el nombre del usuario actual
     */
    public String getUserName() {
        // Priorizar datos locales para usuarios locales
        String localName = preferences.getString(KEY_USER_NAME, null);
        if (localName != null) {
            return localName;
        }
        
        // Fallback a Firebase si no hay datos locales
        /*
        // FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getDisplayName();
        }
        */
        return null;
    }
    
    /**
     * Verifica si el usuario está logueado
     */
    public boolean isLoggedIn() {
        // Priorizar sesión local para usuarios locales
        boolean localLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        
        // Solo verificar Firebase si no hay sesión local
        boolean firebaseLoggedIn = false;
        /*
        if (!localLoggedIn) {
            // FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            firebaseLoggedIn = currentUser != null;
        }
        */
        
        boolean isLoggedIn = localLoggedIn || firebaseLoggedIn;
        android.util.Log.d("SessionManager", "isLoggedIn - Local: " + localLoggedIn + ", Firebase: " + firebaseLoggedIn + ", Result: " + isLoggedIn);
        
        return isLoggedIn;
    }
    
    /**
     * Guarda los datos de sesión del usuario
     */
    public void createSession(String userId, String email, String name) {
        android.util.Log.d("SessionManager", "createSession - UserId: " + userId + ", Email: " + email + ", Name: " + name);
        
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        
        // Verificar que se guardó correctamente
        boolean saved = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        android.util.Log.d("SessionManager", "Sesión guardada correctamente: " + saved);
    }
    
    /**
     * Limpia la sesión del usuario
     */
    public void logout() {
        editor.clear();
        editor.apply();
        // firebaseAuth.signOut();
    }
    
    /**
     * Actualiza los datos del usuario en la sesión
     */
    public void updateUserData(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
}