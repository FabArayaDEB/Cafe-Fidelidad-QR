package com.example.cafefidelidaqrdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Versión simplificada de QRGenerator para funciones básicas
 */
public class QRGenerator {
    
    private static final int QR_SIZE = 512;
    
    /**
     * Genera un código QR para cliente
     */
    public static Bitmap generateClientQR(String clienteId, String nombre, String email, String mcId, int puntos) {
        try {
            String qrContent = String.format(
                "CLIENTE:%s|NOMBRE:%s|EMAIL:%s|MCID:%s|PUNTOS:%d",
                clienteId, nombre, email, mcId, puntos
            );
            
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
            String clienteId = parts[0].split(":")[1];
            String nombre = parts[1].split(":")[1];
            String email = parts[2].split(":")[1];
            String mcId = parts[3].split(":")[1];
            int puntos = Integer.parseInt(parts[4].split(":")[1]);
            
            return new ClienteQRData(clienteId, nombre, email, mcId, puntos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Clase para datos del QR de cliente
     */
    public static class ClienteQRData {
        private String clienteId;
        private String nombre;
        private String email;
        private String mcId;
        private int puntos;
        
        public ClienteQRData(String clienteId, String nombre, String email, String mcId, int puntos) {
            this.clienteId = clienteId;
            this.nombre = nombre;
            this.email = email;
            this.mcId = mcId;
            this.puntos = puntos;
        }
        
        // Getters
        public String getClienteId() { return clienteId; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getMcId() { return mcId; }
        public int getPuntos() { return puntos; }
        
        public String getNivel() {
            // Determinar nivel basado en puntos
            if (puntos >= 1000) return "VIP";
            else if (puntos >= 500) return "Premium";
            else return "Regular";
        }
    }
}