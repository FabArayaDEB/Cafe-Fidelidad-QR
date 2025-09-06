package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de nonces para prevenir reutilización de códigos QR
 * Maneja tanto nonces locales (offline) como sincronizados (online)
 */
public class NonceManager {
    
    private static final String TAG = "NonceManager";
    private static final String PREFS_NAME = "nonce_manager_prefs";
    private static final String KEY_LOCAL_NONCES = "local_nonces";
    private static final String KEY_SYNCED_NONCES = "synced_nonces";
    private static final long NONCE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 horas
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    // Cache en memoria para acceso rápido
    private final Map<String, Long> localNoncesCache = new ConcurrentHashMap<>();
    private final Map<String, Long> syncedNoncesCache = new ConcurrentHashMap<>();
    
    /**
     * Información de un nonce
     */
    public static class NonceInfo {
        public final String nonce;
        public final long timestamp;
        public final boolean isSynced;
        
        public NonceInfo(String nonce, long timestamp, boolean isSynced) {
            this.nonce = nonce;
            this.timestamp = timestamp;
            this.isSynced = isSynced;
        }
    }
    
    public NonceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadNoncesFromStorage();
    }
    
    /**
     * Verifica si un nonce ya fue usado (local o sincronizado)
     */
    public boolean isNonceUsed(String nonce) {
        return isNonceUsedLocally(nonce) || isNonceSynced(nonce);
    }
    
    /**
     * Verifica si un nonce ya fue usado localmente
     */
    public boolean isNonceUsedLocally(String nonce) {
        if (nonce == null || nonce.trim().isEmpty()) {
            return false;
        }
        
        Long timestamp = localNoncesCache.get(nonce);
        if (timestamp == null) {
            return false;
        }
        
        // Verificar si no ha expirado
        long currentTime = System.currentTimeMillis();
        if (currentTime - timestamp > NONCE_EXPIRY_MS) {
            // Nonce expirado, removerlo
            removeLocalNonce(nonce);
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifica si un nonce ya fue sincronizado con el servidor
     */
    public boolean isNonceSynced(String nonce) {
        if (nonce == null || nonce.trim().isEmpty()) {
            return false;
        }
        
        Long timestamp = syncedNoncesCache.get(nonce);
        if (timestamp == null) {
            return false;
        }
        
        // Verificar si no ha expirado
        long currentTime = System.currentTimeMillis();
        if (currentTime - timestamp > NONCE_EXPIRY_MS) {
            // Nonce expirado, removerlo
            removeSyncedNonce(nonce);
            return false;
        }
        
        return true;
    }
    
    /**
     * Marca un nonce como usado localmente (modo offline)
     */
    public void markNonceAsUsedLocally(String nonce, long timestamp) {
        if (nonce == null || nonce.trim().isEmpty()) {
            Log.w(TAG, "Intento de marcar nonce vacío como usado localmente");
            return;
        }
        
        Log.d(TAG, "Marcando nonce como usado localmente: " + nonce);
        localNoncesCache.put(nonce, timestamp);
        saveLocalNoncesToStorage();
    }
    
    /**
     * Marca un nonce como usado y sincronizado (modo online)
     */
    public void markNonceAsUsed(String nonce, long timestamp) {
        if (nonce == null || nonce.trim().isEmpty()) {
            Log.w(TAG, "Intento de marcar nonce vacío como usado");
            return;
        }
        
        Log.d(TAG, "Marcando nonce como usado y sincronizado: " + nonce);
        
        // Remover de locales si existe
        localNoncesCache.remove(nonce);
        
        // Agregar a sincronizados
        syncedNoncesCache.put(nonce, timestamp);
        
        saveNoncesToStorage();
    }
    
    /**
     * Migra un nonce de local a sincronizado
     */
    public void migrateNonceToSynced(String nonce) {
        Long timestamp = localNoncesCache.get(nonce);
        if (timestamp != null) {
            Log.d(TAG, "Migrando nonce de local a sincronizado: " + nonce);
            localNoncesCache.remove(nonce);
            syncedNoncesCache.put(nonce, timestamp);
            saveNoncesToStorage();
        }
    }
    
    /**
     * Obtiene información de un nonce
     */
    public NonceInfo getNonceInfo(String nonce) {
        if (nonce == null || nonce.trim().isEmpty()) {
            return null;
        }
        
        Long localTimestamp = localNoncesCache.get(nonce);
        if (localTimestamp != null) {
            return new NonceInfo(nonce, localTimestamp, false);
        }
        
        Long syncedTimestamp = syncedNoncesCache.get(nonce);
        if (syncedTimestamp != null) {
            return new NonceInfo(nonce, syncedTimestamp, true);
        }
        
        return null;
    }
    
    /**
     * Limpia nonces expirados
     */
    public void cleanupExpiredNonces() {
        Log.d(TAG, "Limpiando nonces expirados");
        
        long currentTime = System.currentTimeMillis();
        int removedLocal = 0;
        int removedSynced = 0;
        
        // Limpiar nonces locales expirados
        Iterator<Map.Entry<String, Long>> localIterator = localNoncesCache.entrySet().iterator();
        while (localIterator.hasNext()) {
            Map.Entry<String, Long> entry = localIterator.next();
            if (currentTime - entry.getValue() > NONCE_EXPIRY_MS) {
                localIterator.remove();
                removedLocal++;
            }
        }
        
        // Limpiar nonces sincronizados expirados
        Iterator<Map.Entry<String, Long>> syncedIterator = syncedNoncesCache.entrySet().iterator();
        while (syncedIterator.hasNext()) {
            Map.Entry<String, Long> entry = syncedIterator.next();
            if (currentTime - entry.getValue() > NONCE_EXPIRY_MS) {
                syncedIterator.remove();
                removedSynced++;
            }
        }
        
        if (removedLocal > 0 || removedSynced > 0) {
            Log.d(TAG, "Nonces expirados removidos - Locales: " + removedLocal + ", Sincronizados: " + removedSynced);
            saveNoncesToStorage();
        }
    }
    
    /**
     * Obtiene el número de nonces locales pendientes de sincronización
     */
    public int getPendingLocalNoncesCount() {
        return localNoncesCache.size();
    }
    
    /**
     * Obtiene el número de nonces sincronizados
     */
    public int getSyncedNoncesCount() {
        return syncedNoncesCache.size();
    }
    
    /**
     * Obtiene todos los nonces locales pendientes
     */
    public Map<String, Long> getPendingLocalNonces() {
        return new HashMap<>(localNoncesCache);
    }
    
    /**
     * Remueve un nonce local específico
     */
    private void removeLocalNonce(String nonce) {
        localNoncesCache.remove(nonce);
        saveLocalNoncesToStorage();
    }
    
    /**
     * Remueve un nonce sincronizado específico
     */
    private void removeSyncedNonce(String nonce) {
        syncedNoncesCache.remove(nonce);
        saveSyncedNoncesToStorage();
    }
    
    /**
     * Carga nonces desde SharedPreferences
     */
    private void loadNoncesFromStorage() {
        try {
            // Cargar nonces locales
            String localNoncesJson = prefs.getString(KEY_LOCAL_NONCES, "{}");
            Type localType = new TypeToken<Map<String, Long>>(){}.getType();
            Map<String, Long> localNonces = gson.fromJson(localNoncesJson, localType);
            if (localNonces != null) {
                localNoncesCache.putAll(localNonces);
            }
            
            // Cargar nonces sincronizados
            String syncedNoncesJson = prefs.getString(KEY_SYNCED_NONCES, "{}");
            Type syncedType = new TypeToken<Map<String, Long>>(){}.getType();
            Map<String, Long> syncedNonces = gson.fromJson(syncedNoncesJson, syncedType);
            if (syncedNonces != null) {
                syncedNoncesCache.putAll(syncedNonces);
            }
            
            Log.d(TAG, "Nonces cargados - Locales: " + localNoncesCache.size() + 
                     ", Sincronizados: " + syncedNoncesCache.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error cargando nonces desde storage", e);
        }
    }
    
    /**
     * Guarda todos los nonces en SharedPreferences
     */
    private void saveNoncesToStorage() {
        saveLocalNoncesToStorage();
        saveSyncedNoncesToStorage();
    }
    
    /**
     * Guarda nonces locales en SharedPreferences
     */
    private void saveLocalNoncesToStorage() {
        try {
            String localNoncesJson = gson.toJson(localNoncesCache);
            prefs.edit().putString(KEY_LOCAL_NONCES, localNoncesJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando nonces locales", e);
        }
    }
    
    /**
     * Guarda nonces sincronizados en SharedPreferences
     */
    private void saveSyncedNoncesToStorage() {
        try {
            String syncedNoncesJson = gson.toJson(syncedNoncesCache);
            prefs.edit().putString(KEY_SYNCED_NONCES, syncedNoncesJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando nonces sincronizados", e);
        }
    }
    
    /**
     * Limpia todos los nonces (usar con precaución)
     */
    public void clearAllNonces() {
        Log.w(TAG, "Limpiando todos los nonces");
        localNoncesCache.clear();
        syncedNoncesCache.clear();
        prefs.edit()
            .remove(KEY_LOCAL_NONCES)
            .remove(KEY_SYNCED_NONCES)
            .apply();
    }
}