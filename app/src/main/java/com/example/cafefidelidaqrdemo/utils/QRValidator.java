package com.example.cafefidelidaqrdemo.utils;

import android.util.Log;
import java.util.regex.Pattern;

/**
 * Validador de códigos QR simplificado
 * Proporciona validaciones básicas para códigos QR
 */
public class QRValidator {
    private static final String TAG = "QRValidator";
    
    // Patrones básicos para validación
    private static final Pattern QR_PATTERN = Pattern.compile("^[A-Za-z0-9+/=\\-_]+$");
    private static final int MIN_QR_LENGTH = 10;
    private static final int MAX_QR_LENGTH = 500;
    
    /**
     * Valida si un código QR tiene formato válido
     */
    public static boolean isValidQRFormat(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            Log.w(TAG, "QR code is null or empty");
            return false;
        }
        
        String trimmedQR = qrCode.trim();
        
        // Verificar longitud
        if (trimmedQR.length() < MIN_QR_LENGTH || trimmedQR.length() > MAX_QR_LENGTH) {
            Log.w(TAG, "QR code length is invalid: " + trimmedQR.length());
            return false;
        }
        
        // Verificar patrón básico
        if (!QR_PATTERN.matcher(trimmedQR).matches()) {
            Log.w(TAG, "QR code format is invalid");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida si un código QR es de cliente
     */
    public static boolean isClienteQR(String qrCode) {
        if (!isValidQRFormat(qrCode)) {
            return false;
        }
        
        // Validación básica para QR de cliente
        // Asumimos que los QR de cliente contienen información específica
        return qrCode.contains("cliente") || qrCode.contains("CLIENT") || 
               qrCode.length() >= 20; // QR más largos suelen ser de cliente
    }
    
    /**
     * Extrae ID de cliente del código QR
     */
    public static String extractClienteId(String qrCode) {
        if (!isClienteQR(qrCode)) {
            return null;
        }
        
        try {
            // Lógica básica de extracción
            // En un caso real, esto dependería del formato específico del QR
            if (qrCode.contains(":")) {
                String[] parts = qrCode.split(":");
                if (parts.length >= 2) {
                    return parts[1].trim();
                }
            }
            
            // Si no hay separador, usar los primeros caracteres
            return qrCode.substring(0, Math.min(10, qrCode.length()));
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting cliente ID from QR", e);
            return null;
        }
    }
    
    /**
     * Valida integridad básica del QR
     */
    public static boolean validateIntegrity(String qrCode) {
        if (!isValidQRFormat(qrCode)) {
            return false;
        }
        
        // Validaciones adicionales básicas
        try {
            // Verificar que no contenga caracteres peligrosos
            if (qrCode.contains("<script>") || qrCode.contains("javascript:") || 
                qrCode.contains("data:") || qrCode.contains("vbscript:")) {
                Log.w(TAG, "QR code contains potentially dangerous content");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating QR integrity", e);
            return false;
        }
    }
    
    /**
     * Sanitiza el código QR removiendo caracteres peligrosos
     */
    public static String sanitizeQR(String qrCode) {
        if (qrCode == null) {
            return null;
        }
        
        return qrCode.trim()
                    .replaceAll("[<>\"'&]", "")
                    .replaceAll("\\s+", " ");
    }
}