package com.example.cafefidelidaqrdemo.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad para gestionar sucursales
 * Utilizada en el módulo de administración para CRUD de sucursales
 */
@Entity(tableName = "sucursales_admin")
public class SucursalAdminEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "nombre")
    private String nombre;
    
    @ColumnInfo(name = "direccion")
    private String direccion;
    
    @ColumnInfo(name = "ciudad")
    private String ciudad;
    
    @ColumnInfo(name = "telefono")
    private String telefono;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "latitud")
    private double latitud;
    
    @ColumnInfo(name = "longitud")
    private double longitud;
    
    @ColumnInfo(name = "horario_apertura")
    private String horarioApertura; // Formato: "08:00"
    
    @ColumnInfo(name = "horario_cierre")
    private String horarioCierre; // Formato: "20:00"
    
    @ColumnInfo(name = "dias_operacion")
    private String diasOperacion; // JSON: ["lunes", "martes", ...]
    
    @ColumnInfo(name = "capacidad_maxima")
    private int capacidadMaxima;
    
    @ColumnInfo(name = "gerente")
    private String gerente;
    
    @ColumnInfo(name = "activo")
    private boolean activo;
    
    @ColumnInfo(name = "fecha_creacion")
    private long fechaCreacion;
    
    @ColumnInfo(name = "fecha_modificacion")
    private long fechaModificacion;
    
    @ColumnInfo(name = "version")
    private int version; // Para control de versiones
    
    @ColumnInfo(name = "creado_por")
    private String creadoPor;
    
    @ColumnInfo(name = "modificado_por")
    private String modificadoPor;
    
    @ColumnInfo(name = "imagen_url")
    private String imagenUrl;
    
    @ColumnInfo(name = "descripcion")
    private String descripcion;
    
    @ColumnInfo(name = "servicios_disponibles")
    private String serviciosDisponibles; // JSON: ["wifi", "estacionamiento", ...]
    
    // Constructor vacío requerido por Room
    public SucursalAdminEntity() {
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaModificacion = System.currentTimeMillis();
        this.activo = true;
        this.version = 1;
        this.capacidadMaxima = 50; // Valor por defecto
    }
    
    // Constructor completo
    public SucursalAdminEntity(String nombre, String direccion, String ciudad, 
                         String telefono, String email, double latitud, double longitud,
                         String horarioApertura, String horarioCierre, String diasOperacion,
                         String gerente, String creadoPor) {
        this();
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.email = email;
        this.latitud = latitud;
        this.longitud = longitud;
        this.horarioApertura = horarioApertura;
        this.horarioCierre = horarioCierre;
        this.diasOperacion = diasOperacion;
        this.gerente = gerente;
        this.creadoPor = creadoPor;
        this.modificadoPor = creadoPor;
    }
    
    // Getters y Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
        updateModificationTime();
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
        updateModificationTime();
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
        updateModificationTime();
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
        updateModificationTime();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        updateModificationTime();
    }
    
    public double getLatitud() {
        return latitud;
    }
    
    public void setLatitud(double latitud) {
        this.latitud = latitud;
        updateModificationTime();
    }
    
    public double getLongitud() {
        return longitud;
    }
    
    public void setLongitud(double longitud) {
        this.longitud = longitud;
        updateModificationTime();
    }
    
    public String getHorarioApertura() {
        return horarioApertura;
    }
    
    public void setHorarioApertura(String horarioApertura) {
        this.horarioApertura = horarioApertura;
        updateModificationTime();
    }
    
    public String getHorarioCierre() {
        return horarioCierre;
    }
    
    public void setHorarioCierre(String horarioCierre) {
        this.horarioCierre = horarioCierre;
        updateModificationTime();
    }
    
    public String getDiasOperacion() {
        return diasOperacion;
    }
    
    public void setDiasOperacion(String diasOperacion) {
        this.diasOperacion = diasOperacion;
        updateModificationTime();
    }
    
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }
    
    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        updateModificationTime();
    }
    
    public String getGerente() {
        return gerente;
    }
    
    public void setGerente(String gerente) {
        this.gerente = gerente;
        updateModificationTime();
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
        updateModificationTime();
    }
    
    public long getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public long getFechaModificacion() {
        return fechaModificacion;
    }
    
    public void setFechaModificacion(long fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getCreadoPor() {
        return creadoPor;
    }
    
    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
    
    public String getModificadoPor() {
        return modificadoPor;
    }
    
    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
        updateModificationTime();
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        updateModificationTime();
    }
    
    public String getServiciosDisponibles() {
        return serviciosDisponibles;
    }
    
    public void setServiciosDisponibles(String serviciosDisponibles) {
        this.serviciosDisponibles = serviciosDisponibles;
        updateModificationTime();
    }
    
    // Métodos de utilidad
    private void updateModificationTime() {
        this.fechaModificacion = System.currentTimeMillis();
        this.version++;
    }
    
    /**
     * Desactiva la sucursal (eliminación lógica)
     */
    public void desactivar(String modificadoPor) {
        this.activo = false;
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    /**
     * Activa la sucursal
     */
    public void activar(String modificadoPor) {
        this.activo = true;
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    /**
     * Actualiza la ubicación de la sucursal
     */
    public void actualizarUbicacion(double latitud, double longitud, String modificadoPor) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    /**
     * Actualiza los horarios de operación
     */
    public void actualizarHorarios(String apertura, String cierre, String diasOperacion, String modificadoPor) {
        this.horarioApertura = apertura;
        this.horarioCierre = cierre;
        this.diasOperacion = diasOperacion;
        this.modificadoPor = modificadoPor;
        updateModificationTime();
    }
    
    /**
     * Calcula la distancia a otra ubicación (aproximada)
     */
    public double calcularDistancia(double otraLatitud, double otraLongitud) {
        double radioTierra = 6371; // Radio de la Tierra en km
        
        double dLat = Math.toRadians(otraLatitud - this.latitud);
        double dLon = Math.toRadians(otraLongitud - this.longitud);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(this.latitud)) * Math.cos(Math.toRadians(otraLatitud)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return radioTierra * c; // Distancia en km
    }
    
    /**
     * Verifica si la sucursal está abierta en un horario específico
     * Nota: Esta es una implementación básica, en producción se necesitaría
     * una lógica más compleja para manejar días de la semana
     */
    public boolean estaAbierta(String horaActual) {
        if (!activo || horarioApertura == null || horarioCierre == null) {
            return false;
        }
        
        try {
            int horaActualInt = Integer.parseInt(horaActual.replace(":", ""));
            int aperturaInt = Integer.parseInt(horarioApertura.replace(":", ""));
            int cierreInt = Integer.parseInt(horarioCierre.replace(":", ""));
            
            return horaActualInt >= aperturaInt && horaActualInt <= cierreInt;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "SucursalEntity{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", direccion='" + direccion + '\'' +
                ", activo=" + activo +
                ", gerente='" + gerente + '\'' +
                ", version=" + version +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SucursalAdminEntity that = (SucursalAdminEntity) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}