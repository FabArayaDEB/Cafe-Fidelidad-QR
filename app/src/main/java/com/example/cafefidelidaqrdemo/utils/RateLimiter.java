package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Gestor de límites de tasa para prevenir fraudes
 * Implementa límites por cliente/sucursal y detección de patrones anómalos
 */
public class RateLimiter {
    
    private static final String TAG = "RateLimiter";
    private static final String PREFS_NAME = "rate_limiter_prefs";
    private static final String KEY_VISIT_RECORDS = "visit_records";
    private static final String KEY_BLOCKED_CLIENTS = "blocked_clients";
    
    // Configuración de límites
    private static final long HOUR_IN_MS = TimeUnit.HOURS.toMillis(1);
    private static final long DAY_IN_MS = TimeUnit.DAYS.toMillis(1);
    private static final int MAX_VISITS_PER_HOUR = 1; // 1 visita por cliente/sucursal por hora
    private static final int MAX_VISITS_PER_DAY = 10; // Máximo 10 visitas por día por cliente
    private static final long CLEANUP_INTERVAL_MS = TimeUnit.HOURS.toMillis(6); // Limpiar cada 6 horas
    
    // Configuración para detección de fraudes
    private static final int SUSPICIOUS_DEVICE_THRESHOLD = 3; // Más de 3 devices diferentes en 1 hora
    private static final long SUSPICIOUS_TIME_WINDOW_MS = HOUR_IN_MS;
    private static final long BLOCK_DURATION_MS = TimeUnit.HOURS.toMillis(24); // Bloqueo por 24 horas
    
    private final SharedPreferences prefs;
    private final Gson gson;
    private long lastCleanupTime;
    
    // Cache en memoria para acceso rápido
    private final Map<String, List<VisitRecord>> visitRecordsCache = new ConcurrentHashMap<>();
    private final Map<Long, BlockedClient> blockedClientsCache = new ConcurrentHashMap<>();
    
    /**
     * Registro de visita para rate limiting
     */
    public static class VisitRecord {
        public final long clienteId;
        public final long sucursalId;
        public final long timestamp;
        public final String deviceId;
        public final String ubicacion;
        
        public VisitRecord(long clienteId, long sucursalId, long timestamp, String deviceId, String ubicacion) {
            this.clienteId = clienteId;
            this.sucursalId = sucursalId;
            this.timestamp = timestamp;
            this.deviceId = deviceId;
            this.ubicacion = ubicacion;
        }
    }
    
    /**
     * Cliente bloqueado temporalmente
     */
    public static class BlockedClient {
        public final long clienteId;
        public final long blockedUntil;
        public final String reason;
        public final long blockedAt;
        
        public BlockedClient(long clienteId, long blockedUntil, String reason, long blockedAt) {
            this.clienteId = clienteId;
            this.blockedUntil = blockedUntil;
            this.reason = reason;
            this.blockedAt = blockedAt;
        }
        
        public boolean isStillBlocked() {
            return System.currentTimeMillis() < blockedUntil;
        }
    }
    
    /**
     * Resultado de verificación de rate limit
     */
    public static class RateLimitResult {
        public final boolean allowed;
        public final String reason;
        public final int remainingVisits;
        public final long nextAllowedTime;
        
        public RateLimitResult(boolean allowed, String reason, int remainingVisits, long nextAllowedTime) {
            this.allowed = allowed;
            this.reason = reason;
            this.remainingVisits = remainingVisits;
            this.nextAllowedTime = nextAllowedTime;
        }
        
        public static RateLimitResult allowed(int remainingVisits) {
            return new RateLimitResult(true, null, remainingVisits, 0);
        }
        
        public static RateLimitResult blocked(String reason, long nextAllowedTime) {
            return new RateLimitResult(false, reason, 0, nextAllowedTime);
        }
    }
    
    public RateLimiter(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.lastCleanupTime = System.currentTimeMillis();
        loadDataFromStorage();
    }
    
    /**
     * Verifica si una visita está permitida según los límites de tasa
     */
    public RateLimitResult checkRateLimit(long clienteId, long sucursalId, String deviceId, String ubicacion) {
        long currentTime = System.currentTimeMillis();
        
        // Limpiar datos expirados periódicamente
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            cleanupExpiredData();
            lastCleanupTime = currentTime;
        }
        
        // 1. Verificar si el cliente está bloqueado
        BlockedClient blockedClient = blockedClientsCache.get(clienteId);
        if (blockedClient != null && blockedClient.isStillBlocked()) {
            Log.w(TAG, "Cliente bloqueado: " + clienteId + " hasta " + blockedClient.blockedUntil);
            return RateLimitResult.blocked(
                "Cliente bloqueado temporalmente: " + blockedClient.reason,
                blockedClient.blockedUntil
            );
        }
        
        // 2. Obtener registros de visitas del cliente
        String key = getVisitKey(clienteId, sucursalId);
        List<VisitRecord> records = visitRecordsCache.getOrDefault(key, new ArrayList<>());
        
        // 3. Filtrar registros dentro de la ventana de tiempo
        List<VisitRecord> recentRecords = filterRecentRecords(records, currentTime);
        
        // 4. Verificar límite por hora (1 visita por cliente/sucursal por hora)
        List<VisitRecord> hourlyRecords = filterRecordsByTimeWindow(recentRecords, currentTime, HOUR_IN_MS);
        if (hourlyRecords.size() >= MAX_VISITS_PER_HOUR) {
            long nextAllowedTime = hourlyRecords.get(0).timestamp + HOUR_IN_MS;
            Log.d(TAG, "Límite por hora excedido para cliente " + clienteId + " en sucursal " + sucursalId);
            return RateLimitResult.blocked(
                "Límite de visitas por hora excedido (máximo " + MAX_VISITS_PER_HOUR + " por hora)",
                nextAllowedTime
            );
        }
        
        // 5. Verificar límite por día
        List<VisitRecord> dailyRecords = filterRecordsByTimeWindow(recentRecords, currentTime, DAY_IN_MS);
        if (dailyRecords.size() >= MAX_VISITS_PER_DAY) {
            long nextAllowedTime = dailyRecords.get(0).timestamp + DAY_IN_MS;
            Log.d(TAG, "Límite diario excedido para cliente " + clienteId);
            return RateLimitResult.blocked(
                "Límite de visitas diarias excedido (máximo " + MAX_VISITS_PER_DAY + " por día)",
                nextAllowedTime
            );
        }
        
        // 6. Detectar patrones sospechosos (múltiples devices)
        if (detectSuspiciousPattern(clienteId, deviceId, currentTime)) {
            blockClient(clienteId, "Patrón sospechoso detectado: múltiples dispositivos", currentTime);
            return RateLimitResult.blocked(
                "Actividad sospechosa detectada. Cliente bloqueado temporalmente.",
                currentTime + BLOCK_DURATION_MS
            );
        }
        
        // 7. Calcular visitas restantes
        int remainingHourly = MAX_VISITS_PER_HOUR - hourlyRecords.size();
        int remainingDaily = MAX_VISITS_PER_DAY - dailyRecords.size();
        int remainingVisits = Math.min(remainingHourly, remainingDaily);
        
        Log.d(TAG, "Rate limit OK para cliente " + clienteId + ". Visitas restantes: " + remainingVisits);
        return RateLimitResult.allowed(remainingVisits);
    }
    
    /**
     * Registra una visita exitosa
     */
    public void recordVisit(long clienteId, long sucursalId, String deviceId, String ubicacion) {
        long currentTime = System.currentTimeMillis();
        
        VisitRecord record = new VisitRecord(clienteId, sucursalId, currentTime, deviceId, ubicacion);
        
        String key = getVisitKey(clienteId, sucursalId);
        List<VisitRecord> records = visitRecordsCache.getOrDefault(key, new ArrayList<>());
        records.add(record);
        
        // Mantener solo registros recientes para optimizar memoria
        List<VisitRecord> recentRecords = filterRecentRecords(records, currentTime);
        visitRecordsCache.put(key, recentRecords);
        
        // Guardar en storage
        saveVisitRecordsToStorage();
        
        Log.d(TAG, "Visita registrada: cliente=" + clienteId + ", sucursal=" + sucursalId + ", device=" + deviceId);
    }
    
    /**
     * Detecta patrones sospechosos (múltiples devices en poco tiempo)
     */
    private boolean detectSuspiciousPattern(long clienteId, String deviceId, long currentTime) {
        // Obtener todas las visitas del cliente en la ventana de tiempo sospechosa
        List<VisitRecord> allClientRecords = new ArrayList<>();
        
        for (List<VisitRecord> records : visitRecordsCache.values()) {
            for (VisitRecord record : records) {
                if (record.clienteId == clienteId && 
                    currentTime - record.timestamp <= SUSPICIOUS_TIME_WINDOW_MS) {
                    allClientRecords.add(record);
                }
            }
        }
        
        // Contar devices únicos
        Map<String, Integer> deviceCounts = new HashMap<>();
        for (VisitRecord record : allClientRecords) {
            deviceCounts.put(record.deviceId, deviceCounts.getOrDefault(record.deviceId, 0) + 1);
        }
        
        // Agregar el device actual
        deviceCounts.put(deviceId, deviceCounts.getOrDefault(deviceId, 0) + 1);
        
        boolean suspicious = deviceCounts.size() > SUSPICIOUS_DEVICE_THRESHOLD;
        
        if (suspicious) {
            Log.w(TAG, "Patrón sospechoso detectado para cliente " + clienteId + 
                     ": " + deviceCounts.size() + " devices diferentes en " + 
                     (SUSPICIOUS_TIME_WINDOW_MS / 1000 / 60) + " minutos");
        }
        
        return suspicious;
    }
    
    /**
     * Bloquea un cliente temporalmente
     */
    private void blockClient(long clienteId, String reason, long currentTime) {
        long blockedUntil = currentTime + BLOCK_DURATION_MS;
        BlockedClient blockedClient = new BlockedClient(clienteId, blockedUntil, reason, currentTime);
        
        blockedClientsCache.put(clienteId, blockedClient);
        saveBlockedClientsToStorage();
        
        Log.w(TAG, "Cliente " + clienteId + " bloqueado hasta " + blockedUntil + ". Razón: " + reason);
    }
    
    /**
     * Filtra registros recientes (últimas 24 horas)
     */
    private List<VisitRecord> filterRecentRecords(List<VisitRecord> records, long currentTime) {
        List<VisitRecord> filtered = new ArrayList<>();
        for (VisitRecord record : records) {
            if (currentTime - record.timestamp <= DAY_IN_MS) {
                filtered.add(record);
            }
        }
        return filtered;
    }
    
    /**
     * Filtra registros por ventana de tiempo específica
     */
    private List<VisitRecord> filterRecordsByTimeWindow(List<VisitRecord> records, long currentTime, long windowMs) {
        List<VisitRecord> filtered = new ArrayList<>();
        for (VisitRecord record : records) {
            if (currentTime - record.timestamp <= windowMs) {
                filtered.add(record);
            }
        }
        return filtered;
    }
    
    /**
     * Genera clave para registros de visita
     */
    private String getVisitKey(long clienteId, long sucursalId) {
        return clienteId + "_" + sucursalId;
    }
    
    /**
     * Limpia datos expirados
     */
    private void cleanupExpiredData() {
        long currentTime = System.currentTimeMillis();
        int removedRecords = 0;
        int removedBlocks = 0;
        
        // Limpiar registros de visitas expirados
        Iterator<Map.Entry<String, List<VisitRecord>>> recordIterator = visitRecordsCache.entrySet().iterator();
        while (recordIterator.hasNext()) {
            Map.Entry<String, List<VisitRecord>> entry = recordIterator.next();
            List<VisitRecord> filteredRecords = filterRecentRecords(entry.getValue(), currentTime);
            
            if (filteredRecords.isEmpty()) {
                recordIterator.remove();
                removedRecords++;
            } else {
                entry.setValue(filteredRecords);
            }
        }
        
        // Limpiar clientes bloqueados expirados
        Iterator<Map.Entry<Long, BlockedClient>> blockIterator = blockedClientsCache.entrySet().iterator();
        while (blockIterator.hasNext()) {
            Map.Entry<Long, BlockedClient> entry = blockIterator.next();
            if (!entry.getValue().isStillBlocked()) {
                blockIterator.remove();
                removedBlocks++;
            }
        }
        
        if (removedRecords > 0 || removedBlocks > 0) {
            Log.d(TAG, "Limpieza completada - Registros removidos: " + removedRecords + 
                     ", Bloqueos expirados: " + removedBlocks);
            saveDataToStorage();
        }
    }
    
    /**
     * Carga datos desde SharedPreferences
     */
    private void loadDataFromStorage() {
        try {
            // Cargar registros de visitas
            String visitRecordsJson = prefs.getString(KEY_VISIT_RECORDS, "{}");
            Type visitType = new TypeToken<Map<String, List<VisitRecord>>>(){}.getType();
            Map<String, List<VisitRecord>> visitRecords = gson.fromJson(visitRecordsJson, visitType);
            if (visitRecords != null) {
                visitRecordsCache.putAll(visitRecords);
            }
            
            // Cargar clientes bloqueados
            String blockedClientsJson = prefs.getString(KEY_BLOCKED_CLIENTS, "{}");
            Type blockedType = new TypeToken<Map<Long, BlockedClient>>(){}.getType();
            Map<Long, BlockedClient> blockedClients = gson.fromJson(blockedClientsJson, blockedType);
            if (blockedClients != null) {
                blockedClientsCache.putAll(blockedClients);
            }
            
            Log.d(TAG, "Datos cargados - Registros: " + visitRecordsCache.size() + 
                     ", Bloqueados: " + blockedClientsCache.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error cargando datos desde storage", e);
        }
    }
    
    /**
     * Guarda todos los datos en SharedPreferences
     */
    private void saveDataToStorage() {
        saveVisitRecordsToStorage();
        saveBlockedClientsToStorage();
    }
    
    /**
     * Guarda registros de visitas
     */
    private void saveVisitRecordsToStorage() {
        try {
            String visitRecordsJson = gson.toJson(visitRecordsCache);
            prefs.edit().putString(KEY_VISIT_RECORDS, visitRecordsJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando registros de visitas", e);
        }
    }
    
    /**
     * Guarda clientes bloqueados
     */
    private void saveBlockedClientsToStorage() {
        try {
            String blockedClientsJson = gson.toJson(blockedClientsCache);
            prefs.edit().putString(KEY_BLOCKED_CLIENTS, blockedClientsJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando clientes bloqueados", e);
        }
    }
    
    /**
     * Obtiene estadísticas del rate limiter
     */
    public String getStats() {
        int totalRecords = 0;
        for (List<VisitRecord> records : visitRecordsCache.values()) {
            totalRecords += records.size();
        }
        
        int activeBlocks = 0;
        for (BlockedClient blocked : blockedClientsCache.values()) {
            if (blocked.isStillBlocked()) {
                activeBlocks++;
            }
        }
        
        return String.format("RateLimit - Registros: %d, Bloqueados: %d", totalRecords, activeBlocks);
    }
    
    /**
     * Desbloquea un cliente manualmente (para administradores)
     */
    public void unblockClient(long clienteId) {
        blockedClientsCache.remove(clienteId);
        saveBlockedClientsToStorage();
        Log.d(TAG, "Cliente " + clienteId + " desbloqueado manualmente");
    }
    
    /**
     * Limpia todos los datos (usar con precaución)
     */
    public void clearAllData() {
        Log.w(TAG, "Limpiando todos los datos del rate limiter");
        visitRecordsCache.clear();
        blockedClientsCache.clear();
        prefs.edit()
            .remove(KEY_VISIT_RECORDS)
            .remove(KEY_BLOCKED_CLIENTS)
            .apply();
    }
}