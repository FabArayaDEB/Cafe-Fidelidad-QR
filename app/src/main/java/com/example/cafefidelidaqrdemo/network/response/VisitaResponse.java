package com.example.cafefidelidaqrdemo.network.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Respuesta de API para operaciones de visitas
 * Clase simplificada para manejar respuestas del servidor
 */
public class VisitaResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private VisitaData data;
    
    @SerializedName("errors")
    private List<String> errors;
    
    // Constructores
    public VisitaResponse() {}
    
    public VisitaResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public VisitaData getData() {
        return data;
    }
    
    public void setData(VisitaData data) {
        this.data = data;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    /**
     * Clase interna para datos de visita
     */
    public static class VisitaData {
        @SerializedName("visitaId")
        private String visitaId;
        
        @SerializedName("clienteId")
        private String clienteId;
        
        @SerializedName("sucursalId")
        private Long sucursalId;
        
        @SerializedName("fecha")
        private String fecha;
        
        @SerializedName("puntosGanados")
        private int puntosGanados;
        
        @SerializedName("estado")
        private String estado;
        
        // Constructores
        public VisitaData() {}
        
        // Getters y Setters
        public String getVisitaId() {
            return visitaId;
        }
        
        public void setVisitaId(String visitaId) {
            this.visitaId = visitaId;
        }
        
        public String getClienteId() {
            return clienteId;
        }
        
        public void setClienteId(String clienteId) {
            this.clienteId = clienteId;
        }
        
        public Long getSucursalId() {
            return sucursalId;
        }
        
        public void setSucursalId(Long sucursalId) {
            this.sucursalId = sucursalId;
        }
        
        public String getFecha() {
            return fecha;
        }
        
        public void setFecha(String fecha) {
            this.fecha = fecha;
        }
        
        public int getPuntosGanados() {
            return puntosGanados;
        }
        
        public void setPuntosGanados(int puntosGanados) {
            this.puntosGanados = puntosGanados;
        }
        
        public String getEstado() {
            return estado;
        }
        
        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
    
    /**
     * Método para obtener progreso (compatibilidad)
     */
    public String getProgreso() {
        if (data != null) {
            return "Puntos ganados: " + data.getPuntosGanados();
        }
        return "Sin progreso disponible";
    }
}