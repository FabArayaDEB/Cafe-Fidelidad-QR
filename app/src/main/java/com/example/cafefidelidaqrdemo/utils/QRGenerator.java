package com.example.cafefidelidaqrdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Generador de códigos QR personales para clientes
 * Incluye información del cliente y validación de seguridad
 */
public class QRGenerator {
    
    private static final int QR_SIZE = 512;
    private static final String SECRET_KEY = "CAFE_FIDELIDAD_2024";
    
    /**
     * Genera un código QR personal para un cliente
     * @param clienteId ID único del cliente
     * @param nombre Nombre del cliente
     * @param email Email del cliente
     * @param mcId McID del cliente
     * @param puntos Puntos actuales del cliente
     * @return Bitmap del código QR generado
     */
    public static Bitmap generateClientQR(String clienteId, String nombre, String email, 
                                         String mcId, int puntos) {
        try {
            // Crear objeto JSON con información del cliente
            JSONObject clientData = new JSONObject();
            clientData.put("type", "CLIENT_QR");
            clientData.put("clienteId", clienteId);
            clientData.put("nombre", nombre);
            clientData.put("email", email);
            clientData.put("mcId", mcId);
            clientData.put("puntos", puntos);
            clientData.put("timestamp", new Date().getTime());
            
            // Generar hash de seguridad
            String securityHash = generateSecurityHash(clienteId, mcId);
            clientData.put("hash", securityHash);
            
            // Convertir a string para el QR
            String qrContent = clientData.toString();
            
            // Generar el código QR
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            
            // Convertir BitMatrix a Bitmap
            Bitmap bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565);
            for (int x = 0; x < QR_SIZE; x++) {
                for (int y = 0; y < QR_SIZE; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bitmap;
            
        } catch (WriterException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Genera un hash de seguridad para validar la autenticidad del QR
     * @param clienteId ID del cliente
     * @param mcId McID del cliente
     * @return Hash de seguridad
     */
    private static String generateSecurityHash(String clienteId, String mcId) {
        try {
            String data = clienteId + mcId + SECRET_KEY;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().substring(0, 16); // Primeros 16 caracteres
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * Valida si un hash de seguridad es correcto
     * @param clienteId ID del cliente
     * @param mcId McID del cliente
     * @param providedHash Hash proporcionado
     * @return true si el hash es válido
     */
    public static boolean validateSecurityHash(String clienteId, String mcId, String providedHash) {
        String expectedHash = generateSecurityHash(clienteId, mcId);
        return expectedHash.equals(providedHash);
    }
    
    /**
     * Valida si un código QR es válido para un cliente
     * @param qrContent Contenido del código QR
     * @return true si es un QR de cliente válido
     */
    public static boolean isValidClientQR(String qrContent) {
        try {
            JSONObject data = new JSONObject(qrContent);
            return "CLIENT_QR".equals(data.optString("type"));
        } catch (JSONException e) {
            return false;
        }
    }
    
    /**
     * Extrae información del cliente desde el contenido de un QR
     * @param qrContent Contenido del código QR
     * @return ClienteQRData con la información del cliente o null si es inválido
     */
    public static ClienteQRData parseClientQR(String qrContent) {
        try {
            JSONObject data = new JSONObject(qrContent);
            
            // Verificar que es un QR de cliente
            if (!"CLIENT_QR".equals(data.optString("type"))) {
                return null;
            }
            
            // Validar hash de seguridad
            String clienteId = data.optString("clienteId");
            String mcId = data.optString("mcId");
            String hash = data.optString("hash");
            
            if (!validateSecurityHash(clienteId, mcId, hash)) {
                return null;
            }
            
            // Crear objeto ClienteQRData
            ClienteQRData clienteData = new ClienteQRData();
            clienteData.setClienteId(clienteId);
            clienteData.setNombre(data.optString("nombre"));
            clienteData.setEmail(data.optString("email"));
            clienteData.setMcId(mcId);
            clienteData.setPuntos(data.optInt("puntos"));
            clienteData.setTimestamp(data.optLong("timestamp"));
            
            return clienteData;
            
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Clase para almacenar datos del cliente extraídos del QR
     */
    public static class ClienteQRData {
        private String clienteId;
        private String nombre;
        private String apellido;
        private String email;
        private String mcId;
        private int puntos;
        private String nivel;
        private long timestamp;
        
        // Getters y Setters
        public String getClienteId() { return clienteId; }
        public void setClienteId(String clienteId) { this.clienteId = clienteId; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { 
            // Separar nombre y apellido si vienen juntos
            if (nombre != null && nombre.contains(" ")) {
                String[] parts = nombre.split(" ", 2);
                this.nombre = parts[0];
                this.apellido = parts.length > 1 ? parts[1] : "";
            } else {
                this.nombre = nombre;
            }
        }
        
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMcId() { return mcId; }
        public void setMcId(String mcId) { this.mcId = mcId; }
        
        public int getPuntos() { return puntos; }
        public void setPuntos(int puntos) { this.puntos = puntos; }
        
        public String getNivel() { return nivel; }
        public void setNivel(String nivel) { this.nivel = nivel; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}