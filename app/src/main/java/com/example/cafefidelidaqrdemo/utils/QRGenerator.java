package com.example.cafefidelidaqrdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Versión simplificada de QRGenerator para funciones básicas
 */
public class QRGenerator {
    
    private static final int QR_SIZE = 512;
    // Nota: En producción, mover la clave a almacenamiento seguro (keystore/config remoto)
    private static final String HMAC_SECRET = "CAFEDI_APP_SECRET_DEMO";
    
    /**
     * Genera un código QR para cliente
     */
    public static Bitmap generateClientQR(String clienteId, String nombre, String email, String mcId) {
        try {
            // Campos dinámicos
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);

            // Sanitizar valores para evitar romper el formato clave:valor
            String safeNombre = sanitize(nombre);
            String safeEmail = sanitize(email);
            String safeMcId = sanitize(mcId);

            // Base de datos a firmar
            String base = String.format(
                    "CLIENTE:%s|NOMBRE:%s|EMAIL:%s|MCID:%s|TS:%d|NONCE:%s",
                    clienteId, safeNombre, safeEmail, safeMcId, timestamp, nonce
            );

            String signature = hmacSha256(base, HMAC_SECRET);
            String qrContent = base + "|SIG:" + signature;
            
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            
            Bitmap bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565);
            for (int x = 0; x < QR_SIZE; x++) {
                for (int y = 0; y < QR_SIZE; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Valida si un QR es de cliente válido
     */
    public static boolean isValidClientQR(String qrContent) {
        return qrContent != null && qrContent.startsWith("CLIENTE:");
    }
    
    /**
     * Parsea los datos de un QR de cliente
     */
    public static ClienteQRData parseClientQR(String qrContent) {
        if (!isValidClientQR(qrContent)) {
            return null;
        }
        
        try {
            String[] parts = qrContent.split("\\|");
            String clienteId = null, nombre = null, email = null, mcId = null;
            for (String p : parts) {
                int idx = p.indexOf(":");
                if (idx <= 0) continue;
                String key = p.substring(0, idx);
                String val = p.substring(idx + 1);
                switch (key) {
                    case "CLIENTE": clienteId = val; break;
                    case "NOMBRE": nombre = val; break;
                    case "EMAIL": email = val; break;
                    case "MCID": mcId = val; break;
                    default: break; // Ignorar TS, NONCE, SIG u otros
                }
            }
            if (clienteId == null) return null;
            return new ClienteQRData(nombrar(clienteId), nombrar(nombre), nombrar(email), nombrar(mcId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replaceAll("[|:]", "_");
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(raw, Base64.NO_WRAP);
        } catch (Exception e) {
            // Fallback: SHA-256 simple si HMAC falla (no recomendado para producción)
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] raw = digest.digest(data.getBytes(StandardCharsets.UTF_8));
                return Base64.encodeToString(raw, Base64.NO_WRAP);
            } catch (Exception ex) {
                return "";
            }
        }
    }

    private static String nombrar(String s) {
        return s == null ? "" : s;
    }
    
    /**
     * Clase para datos del QR de cliente
     */
    public static class ClienteQRData {
        private String clienteId;
        private String nombre;
        private String email;
        private String mcId;
        
        public ClienteQRData(String clienteId, String nombre, String email, String mcId) {
            this.clienteId = clienteId;
            this.nombre = nombre;
            this.email = email;
            this.mcId = mcId;
        }
        
        // Getters
        public String getClienteId() { return clienteId; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getMcId() { return mcId; }
    }
}