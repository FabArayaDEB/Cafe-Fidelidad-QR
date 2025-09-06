package com.example.cafefidelidaqrdemo.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.RateLimiter;
import com.example.cafefidelidaqrdemo.utils.FraudDetector;

import android.location.Location;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

/**
 * Validador de códigos QR con verificación HMAC, TTL y nonce
 * Maneja validación online y offline según disponibilidad de red
 */
public class QRValidator {
    
    private static final String TAG = "QRValidator";
    
    // Configuración de validación
    private static final String QR_PATTERN = "qr://cafe/sucursal/([^/]+)/([^/]+)/([^/]+)/([^/]+)";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long TTL_WINDOW_MS = 5 * 60 * 1000; // 5 minutos
    private static final long OFFLINE_TTL_WINDOW_MS = 2 * 60 * 1000; // 2 minutos para offline
    private static final String SECRET_KEY = "cafe_fidelidad_secret_2024"; // En producción debe venir del servidor
    
    private final Context context;
    private final NonceManager nonceManager;
    private final ApiService apiService;
    private final RateLimiter rateLimiter;
    private final FraudDetector fraudDetector;
    
    public QRValidator(Context context) {
        this.context = context;
        this.nonceManager = new NonceManager(context);
        this.apiService = ApiService.getInstance();
        this.rateLimiter = new RateLimiter(context);
        this.fraudDetector = new FraudDetector(context);
    }
    
    /**
     * Resultado de validación de QR
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String sucursalId;
        public final long timestamp;
        public final String nonce;
        public final String errorMessage;
        public final ValidationError errorType;
        
        public ValidationResult(boolean isValid, String sucursalId, long timestamp, 
                              String nonce, String errorMessage, ValidationError errorType) {
            this.isValid = isValid;
            this.sucursalId = sucursalId;
            this.timestamp = timestamp;
            this.nonce = nonce;
            this.errorMessage = errorMessage;
            this.errorType = errorType;
        }
        
        public static ValidationResult success(String sucursalId, long timestamp, String nonce) {
            return new ValidationResult(true, sucursalId, timestamp, nonce, null, null);
        }
        
        public static ValidationResult error(ValidationError errorType, String message) {
            return new ValidationResult(false, null, 0, null, message, errorType);
        }
    }
    
    /**
     * Tipos de errores de validación
     */
    public enum ValidationError {
        INVALID_FORMAT,
        EXPIRED_QR,
        INVALID_SIGNATURE,
        NONCE_ALREADY_USED,
        RATE_LIMIT_EXCEEDED,
        FRAUD_DETECTED,
        CLOCK_SKEW,
        INVALID_SUCURSAL,
        NETWORK_ERROR
    }
    
    /**
     * Validación completa de QR (online) con prevención de fraudes
     * @param qrContent Contenido del código QR
     * @param deviceId ID del dispositivo
     * @param clienteId ID del cliente
     * @param sucursalId ID de la sucursal
     * @param location Ubicación del dispositivo
     * @return Resultado de validación
     */
    public ValidationResult validateQROnline(String qrContent, String deviceId, long clienteId, long sucursalId, Location location) {
        Log.d(TAG, "Validando QR online: " + qrContent);
        
        try {
            // 1. Validación de formato
            ValidationResult formatResult = validateFormat(qrContent);
            if (!formatResult.isValid) {
                return formatResult;
            }
            
            // 2. Verificar rate limiting
            RateLimiter.RateLimitResult rateLimitResult = rateLimiter.checkRateLimit(
                clienteId, sucursalId, deviceId, location != null ? location.toString() : "unknown"
            );
            
            if (!rateLimitResult.allowed) {
                Log.w(TAG, "Rate limit excedido: " + rateLimitResult.reason);
                return ValidationResult.error(ValidationError.RATE_LIMIT_EXCEEDED, rateLimitResult.reason);
            }
            
            // 3. Análisis de fraude
            FraudDetector.FraudAnalysisResult fraudResult = fraudDetector.analyzeVisit(
                clienteId, sucursalId, qrContent, deviceId, location, formatResult.timestamp
            );
            
            if (fraudResult.isFraudulent) {
                Log.w(TAG, "Fraude detectado - Score: " + fraudResult.riskScore + 
                         ", Patrones: " + fraudResult.detectedPatterns);
                return ValidationResult.error(ValidationError.FRAUD_DETECTED, 
                    "Actividad sospechosa detectada: " + fraudResult.recommendation);
            }
            
            // 4. Verificación de nonce localmente primero
            if (nonceManager.isNonceUsed(formatResult.nonce)) {
                return ValidationResult.error(ValidationError.NONCE_ALREADY_USED, 
                    "Este código QR ya fue utilizado");
            }
            
            // 5. Llamar a la API para validación
            ApiService.QRValidationRequest request = new ApiService.QRValidationRequest(
                qrContent, deviceId, System.currentTimeMillis(), "app"
            );
            
            Response<ApiService.QRValidationResponse> response = apiService.validarQR(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                ApiService.QRValidationResponse apiResponse = response.body();
                
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    // Validación exitosa
                    ApiService.QRValidationData data = apiResponse.getData();
                    
                    // Marcar nonce como usado
                    nonceManager.markNonceAsUsed(formatResult.nonce, formatResult.timestamp);
                    
                    // Registrar visita exitosa en rate limiter
                    rateLimiter.recordVisit(clienteId, sucursalId, deviceId, 
                        location != null ? location.toString() : "unknown");
                    
                    return ValidationResult.success(data.getSucursalId(), data.getTimestamp(), data.getNonce());
                    
                } else {
                    // Error de validación del servidor
                    ValidationError error = mapApiErrorToValidationError(apiResponse.getError());
                    return ValidationResult.error(error, 
                        apiResponse.getMessage() != null ? apiResponse.getMessage() : "Error de validación");
                }
                
            } else {
                // Error de red o servidor
                Log.w(TAG, "Error en respuesta de API: " + response.code());
                
                if (response.code() >= 500) {
                    // Error del servidor, intentar validación offline como fallback
                    Log.d(TAG, "Error del servidor, intentando validación offline");
                    return validateQROffline(qrContent, deviceId, clienteId, sucursalId, location);
                } else {
                    return ValidationResult.error(ValidationError.NETWORK_ERROR, 
                        "Error del servidor: " + response.code());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error validando QR online", e);
            
            // En caso de excepción, intentar validación offline como fallback
            Log.d(TAG, "Excepción en validación online, intentando offline");
            return validateQROffline(qrContent, deviceId, clienteId, sucursalId, location);
        }
    }
    
    /**
     * Validación básica de QR (offline) con prevención de fraudes
     * Solo valida formato y TTL reducido
     * @param qrContent Contenido del código QR
     * @param deviceId ID del dispositivo
     * @param clienteId ID del cliente
     * @param sucursalId ID de la sucursal
     * @param location Ubicación del dispositivo
     * @return Resultado de validación
     */
    public ValidationResult validateQROffline(String qrContent, String deviceId, long clienteId, long sucursalId, Location location) {
        Log.d(TAG, "Validando QR offline: " + qrContent);
        
        // 1. Validación de formato
        ValidationResult formatResult = validateFormat(qrContent);
        if (!formatResult.isValid) {
            return formatResult;
        }
        
        // 2. Validación de TTL más corta para offline
        ValidationResult ttlResult = validateTTL(formatResult.timestamp, OFFLINE_TTL_WINDOW_MS);
        if (!ttlResult.isValid) {
            return ttlResult;
        }
        
        // 3. Verificar rate limiting (reglas locales)
        RateLimiter.RateLimitResult rateLimitResult = rateLimiter.checkRateLimit(
            clienteId, sucursalId, deviceId, location != null ? location.toString() : "unknown"
        );
        
        if (!rateLimitResult.allowed) {
            Log.w(TAG, "Rate limit excedido en modo offline: " + rateLimitResult.reason);
            return ValidationResult.error(ValidationError.RATE_LIMIT_EXCEEDED, rateLimitResult.reason);
        }
        
        // 4. Análisis de fraude offline
        FraudDetector.FraudAnalysisResult fraudResult = fraudDetector.analyzeVisit(
            clienteId, sucursalId, qrContent, deviceId, location, formatResult.timestamp
        );
        
        if (fraudResult.isFraudulent) {
            Log.w(TAG, "Fraude detectado en modo offline - Score: " + fraudResult.riskScore + 
                     ", Patrones: " + fraudResult.detectedPatterns);
            return ValidationResult.error(ValidationError.FRAUD_DETECTED, 
                "Actividad sospechosa detectada (offline): " + fraudResult.recommendation);
        }
        
        // 5. Verificación básica de nonce (solo local)
        if (nonceManager.isNonceUsedLocally(formatResult.nonce)) {
            return ValidationResult.error(ValidationError.NONCE_ALREADY_USED, 
                "Este código QR ya fue escaneado localmente");
        }
        
        // En modo offline, marcamos el nonce como usado localmente
        nonceManager.markNonceAsUsedLocally(formatResult.nonce, formatResult.timestamp);
        
        // Registrar visita exitosa en rate limiter
        rateLimiter.recordVisit(clienteId, sucursalId, deviceId, 
            location != null ? location.toString() : "unknown");
        
        return ValidationResult.success(formatResult.sucursalId, formatResult.timestamp, formatResult.nonce);
    }
    
    /**
     * Valida el formato del QR
     * Formato esperado: qr://cafe/sucursal/{sucursalId}/{timestamp}/{nonce}/{firma}
     */
    private ValidationResult validateFormat(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return ValidationResult.error(ValidationError.INVALID_FORMAT, 
                "Código QR vacío o inválido");
        }
        
        Pattern pattern = Pattern.compile(QR_PATTERN);
        Matcher matcher = pattern.matcher(qrContent);
        
        if (!matcher.matches()) {
            return ValidationResult.error(ValidationError.INVALID_FORMAT, 
                "Formato de código QR no válido");
        }
        
        try {
            String sucursalId = matcher.group(1);
            long timestamp = Long.parseLong(matcher.group(2));
            String nonce = matcher.group(3);
            
            if (sucursalId == null || sucursalId.trim().isEmpty()) {
                return ValidationResult.error(ValidationError.INVALID_SUCURSAL, 
                    "ID de sucursal inválido");
            }
            
            if (nonce == null || nonce.trim().isEmpty()) {
                return ValidationResult.error(ValidationError.INVALID_FORMAT, 
                    "Nonce inválido");
            }
            
            return ValidationResult.success(sucursalId, timestamp, nonce);
            
        } catch (NumberFormatException e) {
            return ValidationResult.error(ValidationError.INVALID_FORMAT, 
                "Timestamp inválido en el código QR");
        }
    }
    
    /**
     * Valida el TTL (Time To Live) del QR
     */
    private ValidationResult validateTTL(long qrTimestamp, long ttlWindow) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - qrTimestamp);
        
        if (timeDiff > ttlWindow) {
            // Verificar si es problema de reloj desfasado
            if (timeDiff > 24 * 60 * 60 * 1000) { // Más de 24 horas
                return ValidationResult.error(ValidationError.CLOCK_SKEW, 
                    "Reloj del dispositivo puede estar desfasado. Verifique la hora del sistema.");
            } else {
                return ValidationResult.error(ValidationError.EXPIRED_QR, 
                    "Código QR expirado. Solicite uno nuevo.");
            }
        }
        
        return ValidationResult.success(null, qrTimestamp, null);
    }
    
    /**
     * Valida la firma HMAC del QR
     */
    private ValidationResult validateSignature(String qrContent) {
        try {
            Pattern pattern = Pattern.compile(QR_PATTERN);
            Matcher matcher = pattern.matcher(qrContent);
            
            if (!matcher.matches()) {
                return ValidationResult.error(ValidationError.INVALID_FORMAT, 
                    "No se puede extraer la firma del QR");
            }
            
            String sucursalId = matcher.group(1);
            String timestamp = matcher.group(2);
            String nonce = matcher.group(3);
            String providedSignature = matcher.group(4);
            
            // Construir el payload para verificar
            String payload = sucursalId + "|" + timestamp + "|" + nonce;
            
            // Calcular HMAC esperado
            String expectedSignature = calculateHMAC(payload, SECRET_KEY);
            
            if (!expectedSignature.equals(providedSignature)) {
                return ValidationResult.error(ValidationError.INVALID_SIGNATURE, 
                    "Firma del código QR inválida");
            }
            
            return ValidationResult.success(null, 0, null);
            
        } catch (Exception e) {
            Log.e(TAG, "Error validando firma HMAC", e);
            return ValidationResult.error(ValidationError.INVALID_SIGNATURE, 
                "Error verificando la autenticidad del código QR");
        }
    }
    
    /**
     * Calcula HMAC-SHA256
     */
    private String calculateHMAC(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
    
    /**
     * Obtiene el ID del dispositivo
     */
    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    
    /**
     * Mapea errores de la API a errores de validación
     */
    private ValidationError mapApiErrorToValidationError(ApiService.ValidationError apiError) {
        if (apiError == null) {
            return ValidationError.NETWORK_ERROR;
        }
        
        String code = apiError.getCode();
        if (code == null) {
            return ValidationError.NETWORK_ERROR;
        }
        
        switch (code.toLowerCase()) {
            case "nonce_used":
            case "nonce_duplicate":
                return ValidationError.NONCE_ALREADY_USED;
            case "ttl_expired":
            case "timestamp_invalid":
                return ValidationError.EXPIRED_QR;
            case "signature_invalid":
            case "hmac_invalid":
                return ValidationError.INVALID_SIGNATURE;
            case "format_invalid":
            case "qr_malformed":
                return ValidationError.INVALID_FORMAT;
            case "network_error":
            case "connection_failed":
                return ValidationError.NETWORK_ERROR;
            default:
                return ValidationError.NETWORK_ERROR;
        }
    }
    
    /**
     * Limpia nonces expirados
     */
    public void cleanupExpiredNonces() {
        nonceManager.cleanupExpiredNonces();
    }
    
    /**
     * Obtiene estadísticas de nonces
     */
    public String getNonceStats() {
        int pendingLocal = nonceManager.getPendingLocalNoncesCount();
        int synced = nonceManager.getSyncedNoncesCount();
        return String.format("Nonces - Pendientes: %d, Sincronizados: %d", pendingLocal, synced);
    }
    
    /**
     * Obtiene estadísticas del validador
     */
    public String getStats() {
        return String.format("QRValidator - %s, %s, %s", 
            nonceManager.getStats(),
            rateLimiter.getStats(),
            fraudDetector.getStats());
    }
    
    /**
     * Obtiene estadísticas detalladas del sistema de prevención de fraudes
     */
    public String getDetailedFraudStats() {
        return String.format("Sistema Anti-Fraude:\n%s\n%s\n%s",
            rateLimiter.getStats(),
            fraudDetector.getStats(),
            nonceManager.getStats());
    }
    
    /**
     * Desbloquea un cliente manualmente (para administradores)
     */
    public void unblockClient(long clienteId) {
        rateLimiter.unblockClient(clienteId);
        Log.d(TAG, "Cliente " + clienteId + " desbloqueado manualmente");
    }
    
    /**
     * Limpia todos los datos de prevención de fraudes (usar con precaución)
     */
    public void clearFraudData() {
        Log.w(TAG, "Limpiando todos los datos de prevención de fraudes");
        rateLimiter.clearAllData();
        nonceManager.clearAllNonces();
    }
    
    /**
     * Verifica si hay problemas de sincronización de reloj
     */
    public boolean hasClockSkewIssues(long serverTimestamp) {
        long localTime = System.currentTimeMillis();
        long timeDiff = Math.abs(localTime - serverTimestamp);
        return timeDiff > 5 * 60 * 1000; // Más de 5 minutos de diferencia
    }
}