package com.example.cafefidelidaqrdemo.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * Respuesta de la API para el registro de visitas
 */
public class VisitaResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("visita_id")
    private String visitaId;
    
    @SerializedName("sucursal_id")
    private String sucursalId;
    
    @SerializedName("sucursal_nombre")
    private String sucursalNombre;
    
    @SerializedName("fecha_registro")
    private String fechaRegistro;
    
    @SerializedName("progreso")
    private String progreso;
    
    @SerializedName("visitas_totales")
    private int visitasTotales;
    
    @SerializedName("visitas_mes")
    private int visitasMes;
    
    @SerializedName("puntos_ganados")
    private int puntosGanados;
    
    @SerializedName("nivel_actual")
    private String nivelActual;
    
    @SerializedName("siguiente_beneficio")
    private SiguienteBeneficio siguienteBeneficio;
    
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
    
    public String getVisitaId() {
        return visitaId;
    }
    
    public void setVisitaId(String visitaId) {
        this.visitaId = visitaId;
    }
    
    public String getSucursalId() {
        return sucursalId;
    }
    
    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }
    
    public String getSucursalNombre() {
        return sucursalNombre;
    }
    
    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }
    
    public String getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public String getProgreso() {
        return progreso;
    }
    
    public void setProgreso(String progreso) {
        this.progreso = progreso;
    }
    
    public int getVisitasTotales() {
        return visitasTotales;
    }
    
    public void setVisitasTotales(int visitasTotales) {
        this.visitasTotales = visitasTotales;
    }
    
    public int getVisitasMes() {
        return visitasMes;
    }
    
    public void setVisitasMes(int visitasMes) {
        this.visitasMes = visitasMes;
    }
    
    public int getPuntosGanados() {
        return puntosGanados;
    }
    
    public void setPuntosGanados(int puntosGanados) {
        this.puntosGanados = puntosGanados;
    }
    
    public String getNivelActual() {
        return nivelActual;
    }
    
    public void setNivelActual(String nivelActual) {
        this.nivelActual = nivelActual;
    }
    
    public SiguienteBeneficio getSiguienteBeneficio() {
        return siguienteBeneficio;
    }
    
    public void setSiguienteBeneficio(SiguienteBeneficio siguienteBeneficio) {
        this.siguienteBeneficio = siguienteBeneficio;
    }
    
    /**
     * Formatear progreso para mostrar al usuario
     */
    public String getProgresoFormateado() {
        if (progreso != null && !progreso.isEmpty()) {
            return progreso;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (visitasTotales > 0) {
            sb.append("Visitas totales: ").append(visitasTotales);
        }
        
        if (visitasMes > 0) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Visitas este mes: ").append(visitasMes);
        }
        
        if (puntosGanados > 0) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Puntos ganados: +").append(puntosGanados);
        }
        
        if (nivelActual != null && !nivelActual.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Nivel actual: ").append(nivelActual);
        }
        
        if (siguienteBeneficio != null) {
            if (sb.length() > 0) sb.append("\n\n");
            sb.append(siguienteBeneficio.getDescripcion());
        }
        
        return sb.toString();
    }
    
    /**
     * Clase para información del siguiente beneficio
     */
    public static class SiguienteBeneficio {
        
        @SerializedName("nombre")
        private String nombre;
        
        @SerializedName("visitas_restantes")
        private int visitasRestantes;
        
        @SerializedName("puntos_restantes")
        private int puntosRestantes;
        
        @SerializedName("descripcion")
        private String descripcion;
        
        // Constructores
        public SiguienteBeneficio() {}
        
        public SiguienteBeneficio(String nombre, int visitasRestantes, int puntosRestantes) {
            this.nombre = nombre;
            this.visitasRestantes = visitasRestantes;
            this.puntosRestantes = puntosRestantes;
        }
        
        // Getters y Setters
        public String getNombre() {
            return nombre;
        }
        
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
        
        public int getVisitasRestantes() {
            return visitasRestantes;
        }
        
        public void setVisitasRestantes(int visitasRestantes) {
            this.visitasRestantes = visitasRestantes;
        }
        
        public int getPuntosRestantes() {
            return puntosRestantes;
        }
        
        public void setPuntosRestantes(int puntosRestantes) {
            this.puntosRestantes = puntosRestantes;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
        
        /**
         * Generar descripción automática si no viene del servidor
         */
        public String getDescripcionGenerada() {
            if (descripcion != null && !descripcion.isEmpty()) {
                return descripcion;
            }
            
            StringBuilder sb = new StringBuilder();
            
            if (nombre != null && !nombre.isEmpty()) {
                sb.append("Próximo beneficio: ").append(nombre);
            }
            
            if (visitasRestantes > 0) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Te faltan ").append(visitasRestantes).append(" visitas");
            }
            
            if (puntosRestantes > 0) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Te faltan ").append(puntosRestantes).append(" puntos");
            }
            
            return sb.toString();
        }
    }
}