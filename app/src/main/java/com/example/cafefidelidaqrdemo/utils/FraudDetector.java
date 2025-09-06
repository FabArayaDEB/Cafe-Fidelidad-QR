package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Detector de fraudes avanzado para QR de visitas
 * Implementa múltiples algoritmos de detección de patrones anómalos
 */
public class FraudDetector {
    
    private static final String TAG = "FraudDetector";
    private static final String PREFS_NAME = "fraud_detector_prefs";
    private static final String KEY_FRAUD_EVENTS = "fraud_events";
    private static final String KEY_DEVICE_FINGERPRINTS = "device_fingerprints";
    private static final String KEY_LOCATION_HISTORY = "location_history";
    
    // Configuración de detección
    private static final long VELOCITY_TIME_WINDOW_MS = TimeUnit.MINUTES.toMillis(30); // 30 minutos
    private static final double MAX_VELOCITY_KMH = 100.0; // Velocidad máxima realista en km/h
    private static final long BURST_TIME_WINDOW_MS = TimeUnit.MINUTES.toMillis(5); // 5 minutos
    private static final int MAX_BURST_VISITS = 3; // Máximo 3 visitas en 5 minutos
    private static final long CLOCK_SKEW_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(10); // 10 minutos
    private static final int MIN_QR_ENTROPY = 20; // Entropía mínima del QR
    
    // Configuración de ubicación
    private static final double MIN_LOCATION_ACCURACY_METERS = 100.0; // Precisión mínima GPS
    private static final double SUSPICIOUS_DISTANCE_METERS = 10000.0; // 10km en poco tiempo
    
    private final SharedPreferences prefs;
    private final Gson gson;
    private final Context context;
    
    // Cache en memoria
    private final List<FraudEvent> fraudEventsCache = new ArrayList<>();
    private final Map<String, DeviceFingerprint> deviceFingerprintsCache = new HashMap<>();
    private final List<LocationRecord> locationHistoryCache = new ArrayList<>();
    
    /**
     * Evento de fraude detectado
     */
    public static class FraudEvent {
        public final long timestamp;
        public final long clienteId;
        public final String fraudType;
        public final String description;
        public final double riskScore;
        public final Map<String, String> metadata;
        
        public FraudEvent(long timestamp, long clienteId, String fraudType, String description, 
                         double riskScore, Map<String, String> metadata) {
            this.timestamp = timestamp;
            this.clienteId = clienteId;
            this.fraudType = fraudType;
            this.description = description;
            this.riskScore = riskScore;
            this.metadata = metadata != null ? metadata : new HashMap<>();
        }
    }
    
    /**
     * Huella digital del dispositivo
     */
    public static class DeviceFingerprint {
        public final String deviceId;
        public final String model;
        public final String osVersion;
        public final String appVersion;
        public final long firstSeen;
        public final long lastSeen;
        public final int visitCount;
        public final List<Long> associatedClients;
        
        public DeviceFingerprint(String deviceId, String model, String osVersion, String appVersion,
                               long firstSeen, long lastSeen, int visitCount, List<Long> associatedClients) {
            this.deviceId = deviceId;
            this.model = model;
            this.osVersion = osVersion;
            this.appVersion = appVersion;
            this.firstSeen = firstSeen;
            this.lastSeen = lastSeen;
            this.visitCount = visitCount;
            this.associatedClients = associatedClients != null ? associatedClients : new ArrayList<>();
        }
    }
    
    /**
     * Registro de ubicación
     */
    public static class LocationRecord {
        public final long clienteId;
        public final long timestamp;
        public final double latitude;
        public final double longitude;
        public final float accuracy;
        public final long sucursalId;
        
        public LocationRecord(long clienteId, long timestamp, double latitude, double longitude,
                            float accuracy, long sucursalId) {
            this.clienteId = clienteId;
            this.timestamp = timestamp;
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.sucursalId = sucursalId;
        }
    }
    
    /**
     * Resultado de análisis de fraude
     */
    public static class FraudAnalysisResult {
        public final boolean isFraudulent;
        public final double riskScore; // 0.0 - 1.0
        public final List<String> detectedPatterns;
        public final String recommendation;
        public final Map<String, Object> details;
        
        public FraudAnalysisResult(boolean isFraudulent, double riskScore, List<String> detectedPatterns,
                                 String recommendation, Map<String, Object> details) {
            this.isFraudulent = isFraudulent;
            this.riskScore = riskScore;
            this.detectedPatterns = detectedPatterns != null ? detectedPatterns : new ArrayList<>();
            this.recommendation = recommendation;
            this.details = details != null ? details : new HashMap<>();
        }
    }
    
    public FraudDetector(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadDataFromStorage();
    }
    
    /**
     * Analiza una visita en busca de patrones fraudulentos
     */
    public FraudAnalysisResult analyzeVisit(long clienteId, long sucursalId, String qrContent,
                                           String deviceId, Location location, long serverTimestamp) {
        
        long currentTime = System.currentTimeMillis();
        List<String> detectedPatterns = new ArrayList<>();
        double totalRiskScore = 0.0;
        Map<String, Object> details = new HashMap<>();
        
        // 1. Análisis de velocidad imposible
        double velocityRisk = analyzeVelocityPattern(clienteId, location, currentTime);
        if (velocityRisk > 0.5) {
            detectedPatterns.add("IMPOSSIBLE_VELOCITY");
            details.put("velocity_risk", velocityRisk);
        }
        totalRiskScore += velocityRisk * 0.3;
        
        // 2. Análisis de ráfagas de visitas
        double burstRisk = analyzeBurstPattern(clienteId, currentTime);
        if (burstRisk > 0.6) {
            detectedPatterns.add("VISIT_BURST");
            details.put("burst_risk", burstRisk);
        }
        totalRiskScore += burstRisk * 0.2;
        
        // 3. Análisis de desajuste de reloj
        double clockSkewRisk = analyzeClockSkew(serverTimestamp, currentTime);
        if (clockSkewRisk > 0.7) {
            detectedPatterns.add("CLOCK_SKEW");
            details.put("clock_skew_risk", clockSkewRisk);
        }
        totalRiskScore += clockSkewRisk * 0.15;
        
        // 4. Análisis de entropía del QR
        double entropyRisk = analyzeQREntropy(qrContent);
        if (entropyRisk > 0.5) {
            detectedPatterns.add("LOW_QR_ENTROPY");
            details.put("entropy_risk", entropyRisk);
        }
        totalRiskScore += entropyRisk * 0.1;
        
        // 5. Análisis de huella digital del dispositivo
        double deviceRisk = analyzeDeviceFingerprint(deviceId, clienteId, currentTime);
        if (deviceRisk > 0.6) {
            detectedPatterns.add("SUSPICIOUS_DEVICE");
            details.put("device_risk", deviceRisk);
        }
        totalRiskScore += deviceRisk * 0.15;
        
        // 6. Análisis de precisión de ubicación
        double locationRisk = analyzeLocationAccuracy(location);
        if (locationRisk > 0.5) {
            detectedPatterns.add("POOR_LOCATION_ACCURACY");
            details.put("location_risk", locationRisk);
        }
        totalRiskScore += locationRisk * 0.1;
        
        // Normalizar score
        totalRiskScore = Math.min(1.0, totalRiskScore);
        
        // Determinar si es fraudulento
        boolean isFraudulent = totalRiskScore > 0.7 || detectedPatterns.size() >= 3;
        
        // Generar recomendación
        String recommendation = generateRecommendation(totalRiskScore, detectedPatterns);
        
        // Registrar evento si es sospechoso
        if (totalRiskScore > 0.5) {
            recordFraudEvent(clienteId, detectedPatterns, totalRiskScore, details);
        }
        
        // Actualizar registros
        updateLocationHistory(clienteId, location, currentTime, sucursalId);
        updateDeviceFingerprint(deviceId, clienteId, currentTime);
        
        Log.d(TAG, String.format("Análisis de fraude - Cliente: %d, Risk: %.2f, Patrones: %s",
                clienteId, totalRiskScore, detectedPatterns.toString()));
        
        return new FraudAnalysisResult(isFraudulent, totalRiskScore, detectedPatterns, recommendation, details);
    }
    
    /**
     * Analiza patrones de velocidad imposible
     */
    private double analyzeVelocityPattern(long clienteId, Location currentLocation, long currentTime) {
        if (currentLocation == null) return 0.0;
        
        // Buscar ubicación anterior del cliente
        LocationRecord lastLocation = null;
        for (int i = locationHistoryCache.size() - 1; i >= 0; i--) {
            LocationRecord record = locationHistoryCache.get(i);
            if (record.clienteId == clienteId && 
                currentTime - record.timestamp <= VELOCITY_TIME_WINDOW_MS) {
                lastLocation = record;
                break;
            }
        }
        
        if (lastLocation == null) return 0.0;
        
        // Calcular distancia y velocidad
        float[] results = new float[1];
        Location.distanceBetween(
            lastLocation.latitude, lastLocation.longitude,
            currentLocation.getLatitude(), currentLocation.getLongitude(),
            results
        );
        
        double distanceMeters = results[0];
        double timeHours = (currentTime - lastLocation.timestamp) / (1000.0 * 60.0 * 60.0);
        
        if (timeHours <= 0) return 0.0;
        
        double velocityKmh = (distanceMeters / 1000.0) / timeHours;
        
        // Calcular riesgo basado en velocidad
        if (velocityKmh > MAX_VELOCITY_KMH) {
            double riskScore = Math.min(1.0, (velocityKmh - MAX_VELOCITY_KMH) / MAX_VELOCITY_KMH);
            Log.w(TAG, String.format("Velocidad sospechosa detectada: %.2f km/h para cliente %d", 
                    velocityKmh, clienteId));
            return riskScore;
        }
        
        return 0.0;
    }
    
    /**
     * Analiza patrones de ráfagas de visitas
     */
    private double analyzeBurstPattern(long clienteId, long currentTime) {
        int recentVisits = 0;
        
        // Contar visitas recientes del cliente
        for (LocationRecord record : locationHistoryCache) {
            if (record.clienteId == clienteId && 
                currentTime - record.timestamp <= BURST_TIME_WINDOW_MS) {
                recentVisits++;
            }
        }
        
        if (recentVisits >= MAX_BURST_VISITS) {
            double riskScore = Math.min(1.0, (double) recentVisits / (MAX_BURST_VISITS * 2));
            Log.w(TAG, String.format("Ráfaga de visitas detectada: %d visitas en %d minutos para cliente %d",
                    recentVisits, BURST_TIME_WINDOW_MS / 60000, clienteId));
            return riskScore;
        }
        
        return 0.0;
    }
    
    /**
     * Analiza desajuste de reloj del servidor/cliente
     */
    private double analyzeClockSkew(long serverTimestamp, long clientTimestamp) {
        long timeDiff = Math.abs(serverTimestamp - clientTimestamp);
        
        if (timeDiff > CLOCK_SKEW_THRESHOLD_MS) {
            double riskScore = Math.min(1.0, (double) timeDiff / (CLOCK_SKEW_THRESHOLD_MS * 3));
            Log.w(TAG, String.format("Desajuste de reloj detectado: %d ms de diferencia", timeDiff));
            return riskScore;
        }
        
        return 0.0;
    }
    
    /**
     * Analiza la entropía del contenido QR
     */
    private double analyzeQREntropy(String qrContent) {
        if (qrContent == null || qrContent.length() < 10) {
            return 1.0; // QR muy corto es sospechoso
        }
        
        // Calcular entropía de Shannon simplificada
        Map<Character, Integer> charCounts = new HashMap<>();
        for (char c : qrContent.toCharArray()) {
            charCounts.put(c, charCounts.getOrDefault(c, 0) + 1);
        }
        
        double entropy = 0.0;
        int length = qrContent.length();
        
        for (int count : charCounts.values()) {
            double probability = (double) count / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        
        // Normalizar entropía (máximo teórico ~6.6 para ASCII)
        double normalizedEntropy = entropy / 6.6;
        
        if (normalizedEntropy < 0.3) { // Entropía muy baja
            double riskScore = 1.0 - normalizedEntropy;
            Log.w(TAG, String.format("Entropía baja en QR: %.2f", normalizedEntropy));
            return riskScore;
        }
        
        return 0.0;
    }
    
    /**
     * Analiza la huella digital del dispositivo
     */
    private double analyzeDeviceFingerprint(String deviceId, long clienteId, long currentTime) {
        DeviceFingerprint fingerprint = deviceFingerprintsCache.get(deviceId);
        
        if (fingerprint == null) {
            // Dispositivo nuevo - riesgo bajo
            return 0.1;
        }
        
        double riskScore = 0.0;
        
        // Verificar si el dispositivo está asociado con muchos clientes
        if (fingerprint.associatedClients.size() > 5) {
            riskScore += 0.6;
            Log.w(TAG, String.format("Dispositivo %s asociado con %d clientes diferentes",
                    deviceId, fingerprint.associatedClients.size()));
        }
        
        // Verificar frecuencia de uso
        long daysSinceFirstSeen = (currentTime - fingerprint.firstSeen) / (24 * 60 * 60 * 1000);
        if (daysSinceFirstSeen > 0) {
            double visitsPerDay = (double) fingerprint.visitCount / daysSinceFirstSeen;
            if (visitsPerDay > 10) { // Más de 10 visitas por día promedio
                riskScore += 0.3;
            }
        }
        
        return Math.min(1.0, riskScore);
    }
    
    /**
     * Analiza la precisión de la ubicación GPS
     */
    private double analyzeLocationAccuracy(Location location) {
        if (location == null) {
            return 0.8; // Sin ubicación es muy sospechoso
        }
        
        float accuracy = location.getAccuracy();
        
        if (accuracy > MIN_LOCATION_ACCURACY_METERS) {
            double riskScore = Math.min(1.0, accuracy / (MIN_LOCATION_ACCURACY_METERS * 2));
            Log.w(TAG, String.format("Precisión GPS baja: %.1f metros", accuracy));
            return riskScore;
        }
        
        return 0.0;
    }
    
    /**
     * Registra un evento de fraude
     */
    private void recordFraudEvent(long clienteId, List<String> patterns, double riskScore, Map<String, Object> details) {
        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            metadata.put(entry.getKey(), entry.getValue().toString());
        }
        
        FraudEvent event = new FraudEvent(
            System.currentTimeMillis(),
            clienteId,
            String.join(",", patterns),
            "Patrones sospechosos detectados",
            riskScore,
            metadata
        );
        
        fraudEventsCache.add(event);
        
        // Mantener solo eventos recientes (últimos 30 días)
        long thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        fraudEventsCache.removeIf(e -> e.timestamp < thirtyDaysAgo);
        
        saveFraudEventsToStorage();
    }
    
    /**
     * Actualiza el historial de ubicaciones
     */
    private void updateLocationHistory(long clienteId, Location location, long timestamp, long sucursalId) {
        if (location == null) return;
        
        LocationRecord record = new LocationRecord(
            clienteId, timestamp, location.getLatitude(), location.getLongitude(),
            location.getAccuracy(), sucursalId
        );
        
        locationHistoryCache.add(record);
        
        // Mantener solo registros recientes (últimas 24 horas)
        long oneDayAgo = timestamp - TimeUnit.DAYS.toMillis(1);
        locationHistoryCache.removeIf(r -> r.timestamp < oneDayAgo);
        
        saveLocationHistoryToStorage();
    }
    
    /**
     * Actualiza la huella digital del dispositivo
     */
    private void updateDeviceFingerprint(String deviceId, long clienteId, long currentTime) {
        DeviceFingerprint existing = deviceFingerprintsCache.get(deviceId);
        
        if (existing == null) {
            // Crear nueva huella digital
            List<Long> clients = new ArrayList<>();
            clients.add(clienteId);
            
            DeviceFingerprint newFingerprint = new DeviceFingerprint(
                deviceId,
                android.os.Build.MODEL,
                android.os.Build.VERSION.RELEASE,
                "1.0", // Versión de la app
                currentTime,
                currentTime,
                1,
                clients
            );
            
            deviceFingerprintsCache.put(deviceId, newFingerprint);
        } else {
            // Actualizar huella existente
            if (!existing.associatedClients.contains(clienteId)) {
                existing.associatedClients.add(clienteId);
            }
            
            DeviceFingerprint updated = new DeviceFingerprint(
                existing.deviceId,
                existing.model,
                existing.osVersion,
                existing.appVersion,
                existing.firstSeen,
                currentTime,
                existing.visitCount + 1,
                existing.associatedClients
            );
            
            deviceFingerprintsCache.put(deviceId, updated);
        }
        
        saveDeviceFingerprintsToStorage();
    }
    
    /**
     * Genera recomendación basada en el análisis
     */
    private String generateRecommendation(double riskScore, List<String> patterns) {
        if (riskScore > 0.8) {
            return "BLOQUEAR - Riesgo muy alto de fraude";
        } else if (riskScore > 0.6) {
            return "REVISAR - Actividad sospechosa detectada";
        } else if (riskScore > 0.4) {
            return "MONITOREAR - Patrones inusuales";
        } else {
            return "PERMITIR - Actividad normal";
        }
    }
    
    /**
     * Obtiene estadísticas del detector de fraudes
     */
    public String getStats() {
        int recentEvents = 0;
        long oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        
        for (FraudEvent event : fraudEventsCache) {
            if (event.timestamp > oneDayAgo) {
                recentEvents++;
            }
        }
        
        return String.format("FraudDetector - Eventos 24h: %d, Dispositivos: %d, Ubicaciones: %d",
                recentEvents, deviceFingerprintsCache.size(), locationHistoryCache.size());
    }
    
    // Métodos de persistencia
    private void loadDataFromStorage() {
        try {
            // Cargar eventos de fraude
            String fraudEventsJson = prefs.getString(KEY_FRAUD_EVENTS, "[]");
            Type fraudType = new TypeToken<List<FraudEvent>>(){}.getType();
            List<FraudEvent> fraudEvents = gson.fromJson(fraudEventsJson, fraudType);
            if (fraudEvents != null) {
                fraudEventsCache.addAll(fraudEvents);
            }
            
            // Cargar huellas digitales
            String fingerprintsJson = prefs.getString(KEY_DEVICE_FINGERPRINTS, "{}");
            Type fingerprintType = new TypeToken<Map<String, DeviceFingerprint>>(){}.getType();
            Map<String, DeviceFingerprint> fingerprints = gson.fromJson(fingerprintsJson, fingerprintType);
            if (fingerprints != null) {
                deviceFingerprintsCache.putAll(fingerprints);
            }
            
            // Cargar historial de ubicaciones
            String locationJson = prefs.getString(KEY_LOCATION_HISTORY, "[]");
            Type locationType = new TypeToken<List<LocationRecord>>(){}.getType();
            List<LocationRecord> locations = gson.fromJson(locationJson, locationType);
            if (locations != null) {
                locationHistoryCache.addAll(locations);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error cargando datos del detector de fraudes", e);
        }
    }
    
    private void saveFraudEventsToStorage() {
        try {
            String json = gson.toJson(fraudEventsCache);
            prefs.edit().putString(KEY_FRAUD_EVENTS, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando eventos de fraude", e);
        }
    }
    
    private void saveDeviceFingerprintsToStorage() {
        try {
            String json = gson.toJson(deviceFingerprintsCache);
            prefs.edit().putString(KEY_DEVICE_FINGERPRINTS, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando huellas digitales", e);
        }
    }
    
    private void saveLocationHistoryToStorage() {
        try {
            String json = gson.toJson(locationHistoryCache);
            prefs.edit().putString(KEY_LOCATION_HISTORY, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error guardando historial de ubicaciones", e);
        }
    }
}