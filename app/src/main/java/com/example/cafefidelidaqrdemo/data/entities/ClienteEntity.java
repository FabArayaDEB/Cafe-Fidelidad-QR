package com.example.cafefidelidaqrdemo.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import java.util.Date;

/**
 * Entidad para almacenar información del cliente
 */
@Entity(tableName = "clientes")
public class ClienteEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "cliente_id")
    private String clienteId;
    
    @ColumnInfo(name = "nombre")
    private String nombre;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "telefono")
    private String telefono;
    
    @ColumnInfo(name = "puntos_actuales")
    private int puntosActuales;
    
    @ColumnInfo(name = "nivel_fidelidad")
    private String nivelFidelidad;
    
    @ColumnInfo(name = "fecha_registro")
    private Date fechaRegistro;
    
    @ColumnInfo(name = "ultima_actualizacion")
    private Date ultimaActualizacion;
    
    @ColumnInfo(name = "activo")
    private boolean activo;
    
    // Constructor vacío requerido por Room
    public ClienteEntity() {}
    
    // Constructor completo
    public ClienteEntity(@NonNull String clienteId, String nombre, String email, 
                        String telefono, int puntosActuales, String nivelFidelidad, 
                        Date fechaRegistro, Date ultimaActualizacion, boolean activo) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.puntosActuales = puntosActuales;
        this.nivelFidelidad = nivelFidelidad;
        this.fechaRegistro = fechaRegistro;
        this.ultimaActualizacion = ultimaActualizacion;
        this.activo = activo;
    }
    
    // Getters y Setters
    @NonNull
    public String getClienteId() { return clienteId; }
    public void setClienteId(@NonNull String clienteId) { this.clienteId = clienteId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public int getPuntosActuales() { return puntosActuales; }
    
    public void setPuntosActuales(int puntosActuales) { this.puntosActuales = puntosActuales; }
    
    // Métodos alias para compatibilidad
    public int getPuntos() { return puntosActuales; }
    public void setPuntos(int puntos) { this.puntosActuales = puntos; }
    
    public String getNivelFidelidad() { return nivelFidelidad; }
    public void setNivelFidelidad(String nivelFidelidad) { this.nivelFidelidad = nivelFidelidad; }
    
    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public Date getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(Date ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    /**
     * Genera y retorna un McID único basado en los datos del cliente
     * @return McID generado
     */
    public String getMcId() {
        if (nombre == null || email == null) {
            return "MC000";
        }
        
        // Generar McID basado en nombre y email
        String nombreUpper = nombre.toUpperCase();
        String emailLower = email.toLowerCase();
        
        // Tomar primera letra del nombre
        String iniciales = "";
        if (nombreUpper.length() > 0) iniciales += nombreUpper.charAt(0);
        
        // Tomar parte del email antes del @
        String emailPart = emailLower.split("@")[0];
        if (emailPart.length() > 4) {
            emailPart = emailPart.substring(0, 4);
        }
        
        return "MC" + iniciales + emailPart.toUpperCase();
    }
    
    // Métodos de utilidad
    public boolean puedeCanjeaBeneficio(int puntosRequeridos) {
        return activo && puntosActuales >= puntosRequeridos;
    }
    
    public String getNivelSiguiente() {
        switch (nivelFidelidad != null ? nivelFidelidad : "Bronce") {
            case "Bronce": return "Plata";
            case "Plata": return "Oro";
            case "Oro": return "Platino";
            case "Platino": return "Diamante";
            default: return "Bronce";
        }
    }
    
    public int getPuntosParaSiguienteNivel() {
        switch (nivelFidelidad != null ? nivelFidelidad : "Bronce") {
            case "Bronce": return Math.max(0, 1000 - puntosActuales);
            case "Plata": return Math.max(0, 2500 - puntosActuales);
            case "Oro": return Math.max(0, 5000 - puntosActuales);
            case "Platino": return Math.max(0, 10000 - puntosActuales);
            default: return 0;
        }
    }
}