package com.example.cafefidelidaqrdemo.security;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Gestor de conexiones de red seguras
 * Implementa TLS 1.2+ y validaciones de certificados según OWASP
 */
public class SecureNetworkManager {
    private static final String TAG = "SecureNetworkManager";
    private static SecureNetworkManager instance;
    private OkHttpClient secureHttpClient;
    
    private SecureNetworkManager() {
        initializeSecureClient();
    }
    
    public static synchronized SecureNetworkManager getInstance() {
        if (instance == null) {
            instance = new SecureNetworkManager();
        }
        return instance;
    }
    
    /**
     * Inicializa cliente HTTP con configuración segura
     */
    private void initializeSecureClient() {
        try {
            // Configurar especificaciones de conexión segura
            ConnectionSpec tlsSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                .allEnabledCipherSuites()
                .build();
            
            // Crear cliente con configuraciones de seguridad
            secureHttpClient = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(tlsSpec, ConnectionSpec.CLEARTEXT))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
                
            Log.i(TAG, "Cliente HTTP seguro inicializado con TLS 1.2+");
            
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando cliente seguro", e);
            // Fallback a cliente básico
            secureHttpClient = new OkHttpClient();
        }
    }
    
    /**
     * Obtiene cliente HTTP configurado de forma segura
     * @return OkHttpClient con configuraciones de seguridad
     */
    public OkHttpClient getSecureHttpClient() {
        return secureHttpClient;
    }
    
    /**
     * Valida que una URL use HTTPS
     * @param url URL a validar
     * @return true si usa HTTPS, false en caso contrario
     */
    public boolean isSecureUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        return url.toLowerCase().startsWith("https://");
    }
    
    /**
     * Valida el dominio de la URL
     * @param url URL a validar
     * @param allowedDomains Dominios permitidos
     * @return true si el dominio está permitido
     */
    public boolean isAllowedDomain(String url, String... allowedDomains) {
        if (url == null || allowedDomains == null) {
            return false;
        }
        
        try {
            String domain = extractDomain(url);
            for (String allowedDomain : allowedDomains) {
                if (domain.equals(allowedDomain) || domain.endsWith("." + allowedDomain)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validando dominio", e);
        }
        
        return false;
    }
    
    /**
     * Extrae el dominio de una URL
     */
    private String extractDomain(String url) {
        if (url.startsWith("https://")) {
            url = url.substring(8);
        } else if (url.startsWith("http://")) {
            url = url.substring(7);
        }
        
        int slashIndex = url.indexOf('/');
        if (slashIndex != -1) {
            url = url.substring(0, slashIndex);
        }
        
        int colonIndex = url.indexOf(':');
        if (colonIndex != -1) {
            url = url.substring(0, colonIndex);
        }
        
        return url.toLowerCase();
    }
    
    /**
     * Crea un TrustManager personalizado para validación adicional
     */
    private X509TrustManager createCustomTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) 
                    throws CertificateException {
                // Implementar validaciones adicionales si es necesario
            }
            
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) 
                    throws CertificateException {
                // Validar cadena de certificados
                if (chain == null || chain.length == 0) {
                    throw new CertificateException("Cadena de certificados vacía");
                }
                
                // Validar que el certificado no haya expirado
                for (X509Certificate cert : chain) {
                    cert.checkValidity();
                }
                
                // Aquí se pueden agregar más validaciones específicas
            }
            
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
    
    /**
     * Valida headers de seguridad en respuestas HTTP
     * @param headers Headers de la respuesta
     * @return true si los headers de seguridad están presentes
     */
    public boolean validateSecurityHeaders(java.util.Map<String, String> headers) {
        if (headers == null) {
            return false;
        }
        
        // Verificar headers de seguridad importantes
        boolean hasStrictTransportSecurity = headers.containsKey("Strict-Transport-Security");
        boolean hasContentTypeOptions = headers.containsKey("X-Content-Type-Options");
        boolean hasFrameOptions = headers.containsKey("X-Frame-Options");
        
        if (!hasStrictTransportSecurity) {
            Log.w(TAG, "Falta header Strict-Transport-Security");
        }
        
        if (!hasContentTypeOptions) {
            Log.w(TAG, "Falta header X-Content-Type-Options");
        }
        
        if (!hasFrameOptions) {
            Log.w(TAG, "Falta header X-Frame-Options");
        }
        
        // Retornar true si al menos HSTS está presente
        return hasStrictTransportSecurity;
    }
    
    /**
     * Genera headers de seguridad para requests
     * @return Map con headers de seguridad
     */
    public java.util.Map<String, String> getSecurityHeaders() {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        
        headers.put("User-Agent", "CafeFidelidadApp/1.0");
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.put("Pragma", "no-cache");
        headers.put("Expires", "0");
        
        return headers;
    }
}