package com.example.cafefidelidaqrdemo.utils;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitor de rendimiento para medir y reportar métricas de la aplicación
 * Ayuda a cumplir con los requisitos de rendimiento:
 * - Registro de visita <1.5s online
 * - <200ms cache local
 */
public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    
    // Umbrales de rendimiento
    public static final long THRESHOLD_CACHE_LOCAL = 200; // 200ms
    public static final long THRESHOLD_ONLINE = 1500; // 1.5s
    
    // Métricas acumuladas
    private static final Map<String, Long> totalTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> operationCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> maxTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> minTimes = new ConcurrentHashMap<>();
    
    /**
     * Inicia medición de una operación
     */
    public static long startMeasurement(String operationName) {
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "Iniciando medición: " + operationName);
        return startTime;
    }
    
    /**
     * Finaliza medición y reporta resultados
     */
    public static void endMeasurement(String operationName, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        endMeasurement(operationName, startTime, duration);
    }
    
    /**
     * Finaliza medición con duración específica
     */
    public static void endMeasurement(String operationName, long startTime, long duration) {
        // Actualizar estadísticas
        totalTimes.merge(operationName, duration, Long::sum);
        operationCounts.merge(operationName, 1, Integer::sum);
        
        // Actualizar máximo
        maxTimes.merge(operationName, duration, Long::max);
        
        // Actualizar mínimo
        minTimes.merge(operationName, duration, (existing, newVal) -> 
            existing == null ? newVal : Math.min(existing, newVal));
        
        // Determinar umbral según tipo de operación
        long threshold = getThresholdForOperation(operationName);
        
        // Log del resultado
        if (duration > threshold) {
            Log.w(TAG, String.format("⚠️ %s LENTO: %dms > %dms (umbral)", 
                operationName, duration, threshold));
        } else {
            Log.d(TAG, String.format("✅ %s: %dms (< %dms)", 
                operationName, duration, threshold));
        }
    }
    
    /**
     * Obtiene umbral según el tipo de operación
     */
    private static long getThresholdForOperation(String operationName) {
        if (operationName.toLowerCase().contains("cache") || 
            operationName.toLowerCase().contains("local") ||
            operationName.toLowerCase().contains("memoria")) {
            return THRESHOLD_CACHE_LOCAL;
        }
        return THRESHOLD_ONLINE;
    }
    
    /**
     * Reporta estadísticas acumuladas
     */
    public static void reportStatistics() {
        Log.i(TAG, "=== REPORTE DE RENDIMIENTO ===");
        
        for (String operation : operationCounts.keySet()) {
            int count = operationCounts.get(operation);
            long total = totalTimes.get(operation);
            long max = maxTimes.get(operation);
            long min = minTimes.get(operation);
            long avg = total / count;
            long threshold = getThresholdForOperation(operation);
            
            String status = avg <= threshold ? "✅ OK" : "⚠️ LENTO";
            
            Log.i(TAG, String.format("%s %s: Promedio=%dms, Min=%dms, Max=%dms, Operaciones=%d", 
                status, operation, avg, min, max, count));
        }
        
        Log.i(TAG, "================================");
    }
    
    /**
     * Limpia todas las estadísticas
     */
    public static void clearStatistics() {
        totalTimes.clear();
        operationCounts.clear();
        maxTimes.clear();
        minTimes.clear();
        Log.d(TAG, "Estadísticas de rendimiento limpiadas");
    }
    
    /**
     * Obtiene promedio de una operación específica
     */
    public static long getAverageTime(String operationName) {
        Integer count = operationCounts.get(operationName);
        Long total = totalTimes.get(operationName);
        
        if (count == null || total == null || count == 0) {
            return 0;
        }
        
        return total / count;
    }
    
    /**
     * Verifica si una operación cumple con los requisitos de rendimiento
     */
    public static boolean meetsPerformanceRequirements(String operationName) {
        long avgTime = getAverageTime(operationName);
        long threshold = getThresholdForOperation(operationName);
        return avgTime <= threshold;
    }
}