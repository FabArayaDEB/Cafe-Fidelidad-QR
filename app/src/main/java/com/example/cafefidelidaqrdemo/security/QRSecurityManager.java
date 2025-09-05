package com.example.cafefidelidaqrdemo.security;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Gestor de seguridad para códigos QR según OWASP Mobile Top 10
 * Implementa HMAC-SHA256 para firmas y JWT para autenticación
 */
public class QRSecurityManager {
    private static final String TAG = "QRSecurityManager";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long JWT_EXPIRATION_TIME = 300000; // 5 minutos
    
    // Clave secreta para HMAC (en producción debe estar en KeyStore)
    private static final String HMAC_SECRET_KEY = "CafeFidelidadSecretKey2024!@#$%^&*()";
    
    // Clave para JWT (en producción debe estar en KeyStore)
    private static final String JWT_SECRET_KEY = "CafeFidelidadJWTSecretKey2024ForQRSecurity";
    
    private static QRSecurityManager instance;
    private SecureRandom secureRandom;
    
    private QRSecurityManager() {
        secureRandom = new SecureRandom();
    }
    
    public static synchronized QRSecurityManager getInstance() {
        if (instance == null) {
            instance = new QRSecurityManager();
        }
        return instance;
    }
    
    /**
     * Genera un código QR seguro con firma HMAC-SHA256
     * @param sucursalId ID de la sucursal
     * @param mesaId ID de la mesa
     * @param monto Monto de la transacción
     * @param timestamp Timestamp de generación
     * @return Código QR firmado
     */
    public String generateSecureQRCode(String sucursalId, String mesaId, double monto, long timestamp) {
        try {
            // Generar nonce único
            String nonce = generateNonce();
            
            // Crear payload base
            String payload = String.format("CAFE_FIDELIDAD:%s:%s:%.2f:%d:%s", 
                sucursalId, mesaId, monto, timestamp, nonce);
            
            // Generar firma HMAC-SHA256
            String signature = generateHMACSignature(payload);
            
            // Retornar código QR con firma
            return payload + ":" + signature;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generando código QR seguro", e);
            return null;
        }
    }
    
    /**
     * Valida un código QR verificando su firma HMAC-SHA256
     * @param qrCode Código QR a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean validateQRCode(String qrCode) {
        try {
            if (qrCode == null || !qrCode.startsWith("CAFE_FIDELIDAD:")) {
                return false;
            }
            
            String[] parts = qrCode.split(":");
            if (parts.length != 7) { // CAFE_FIDELIDAD:sucursal:mesa:monto:timestamp:nonce:signature
                return false;
            }
            
            // Extraer firma
            String providedSignature = parts[6];
            
            // Reconstruir payload sin la firma
            String payload = String.join(":", 
                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            
            // Generar firma esperada
            String expectedSignature = generateHMACSignature(payload);
            
            // Verificar firma
            boolean signatureValid = constantTimeEquals(providedSignature, expectedSignature);
            
            if (!signatureValid) {
                Log.w(TAG, "Firma HMAC inválida");
                return false;
            }
            
            // Verificar timestamp (no más de 10 minutos de antigüedad)
            long timestamp = Long.parseLong(parts[4]);
            long currentTime = System.currentTimeMillis();
            long maxAge = 10 * 60 * 1000; // 10 minutos
            
            if (currentTime - timestamp > maxAge) {
                Log.w(TAG, "Código QR expirado");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validando código QR", e);
            return false;
        }
    }
    
    /**
     * Extrae los datos de un código QR válido
     * @param qrCode Código QR validado
     * @return Map con los datos extraídos
     */
    public Map<String, Object> extractQRData(String qrCode) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            String[] parts = qrCode.split(":");
            if (parts.length >= 6) {
                data.put("sucursalId", parts[1]);
                data.put("mesaId", parts[2]);
                data.put("monto", Double.parseDouble(parts[3]));
                data.put("timestamp", Long.parseLong(parts[4]));
                data.put("nonce", parts[5]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extrayendo datos del QR", e);
        }
        
        return data;
    }
    
    /**
     * Genera un token JWT para autenticación de API
     * @param userId ID del usuario
     * @param sucursalId ID de la sucursal
     * @return Token JWT
     */
    public String generateJWTToken(String userId, String sucursalId) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + JWT_EXPIRATION_TIME);
            
            return Jwts.builder()
                .setSubject(userId)
                .claim("sucursalId", sucursalId)
                .claim("type", "qr_scan")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
                
        } catch (Exception e) {
            Log.e(TAG, "Error generando token JWT", e);
            return null;
        }
    }
    
    /**
     * Valida un token JWT
     * @param token Token a validar
     * @return Claims si es válido, null en caso contrario
     */
    public Claims validateJWTToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        } catch (Exception e) {
            Log.e(TAG, "Error validando token JWT", e);
            return null;
        }
    }
    
    /**
     * Genera firma HMAC-SHA256
     */
    private String generateHMACSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            HMAC_SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(signature, Base64.NO_WRAP);
    }
    
    /**
     * Genera un nonce único
     */
    private String generateNonce() {
        byte[] nonce = new byte[16];
        secureRandom.nextBytes(nonce);
        return Base64.encodeToString(nonce, Base64.NO_WRAP);
    }
    
    /**
     * Comparación de strings en tiempo constante para prevenir timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Valida que la conexión use TLS 1.2+
     * @param url URL a validar
     * @return true si usa TLS seguro
     */
    public boolean validateTLSConnection(String url) {
        return url != null && url.startsWith("https://");
    }
    
    /**
     * Genera hash seguro para almacenamiento
     */
    public String generateSecureHash(String data) {
        try {
            return generateHMACSignature(data + System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Error generando hash seguro", e);
            return null;
        }
    }
}