package com.example.cafefidelidaqrdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;

import java.util.List;

/**
 * DAO para operaciones CRUD de sucursales
 * Utilizado en el módulo de administración
 */
@Dao
public interface SucursalAdminDao {
    
    // ========== OPERACIONES BÁSICAS CRUD ==========
    
    /**
     * Inserta una nueva sucursal
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSucursal(SucursalEntity sucursal);
    
    /**
     * Inserta múltiples sucursales
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertSucursales(List<SucursalEntity> sucursales);
    
    /**
     * Actualiza una sucursal existente
     */
    @Update
    int updateSucursal(SucursalEntity sucursal);
    
    /**
     * Elimina una sucursal (eliminación física - usar con precaución)
     */
    @Delete
    int deleteSucursal(SucursalEntity sucursal);
    
    // ========== CONSULTAS DE LECTURA ==========
    
    /**
     * Obtiene todas las sucursales (activas e inactivas)
     */
    @Query("SELECT * FROM sucursales ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getAllSucursales();
    
    /**
     * Obtiene todas las sucursales de forma síncrona
     */
    @Query("SELECT * FROM sucursales ORDER BY nombre ASC")
    List<SucursalEntity> getAllSucursalesSync();
    
    /**
     * Obtiene solo sucursales activas
     */
    @Query("SELECT * FROM sucursales WHERE activo = 1 ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesActivas();
    
    /**
     * Obtiene solo sucursales inactivas
     */
    @Query("SELECT * FROM sucursales WHERE activo = 0 ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesInactivas();
    
    /**
     * Obtiene una sucursal por ID
     */
    @Query("SELECT * FROM sucursales WHERE id = :id")
    LiveData<SucursalEntity> getSucursalById(long id);
    
    /**
     * Obtiene una sucursal por ID de forma síncrona
     */
    @Query("SELECT * FROM sucursales WHERE id = :id")
    SucursalEntity getSucursalByIdSync(long id);
    
    // ========== CONSULTAS DE BÚSQUEDA ==========
    
    /**
     * Busca sucursales por nombre (búsqueda parcial)
     */
    @Query("SELECT * FROM sucursales WHERE nombre LIKE '%' || :nombre || '%' AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> buscarSucursalesPorNombre(String nombre);
    
    /**
     * Busca sucursales por ciudad
     */
    @Query("SELECT * FROM sucursales WHERE LOWER(ciudad) = LOWER(:ciudad) AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesPorCiudad(String ciudad);
    
    /**
     * Obtiene todas las ciudades únicas
     */
    @Query("SELECT DISTINCT ciudad FROM sucursales WHERE activo = 1 ORDER BY ciudad ASC")
    LiveData<List<String>> getCiudades();
    
    /**
     * Busca sucursales por gerente
     */
    @Query("SELECT * FROM sucursales WHERE LOWER(gerente) LIKE '%' || LOWER(:gerente) || '%' AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesPorGerente(String gerente);
    
    // ========== CONSULTAS GEOGRÁFICAS ==========
    
    /**
     * Obtiene sucursales en un rango de coordenadas (búsqueda por área rectangular)
     */
    @Query("SELECT * FROM sucursales WHERE activo = 1 AND " +
           "latitud BETWEEN :latitudMin AND :latitudMax AND " +
           "longitud BETWEEN :longitudMin AND :longitudMax " +
           "ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesEnArea(double latitudMin, double latitudMax, 
                                                      double longitudMin, double longitudMax);
    
    /**
     * Obtiene sucursales ordenadas por distancia aproximada a un punto
     * Nota: Esta es una aproximación simple, para cálculos precisos usar funciones geográficas
     */
    @Query("SELECT *, " +
           "((latitud - :latitud) * (latitud - :latitud) + (longitud - :longitud) * (longitud - :longitud)) as distancia_aprox " +
           "FROM sucursales WHERE activo = 1 " +
           "ORDER BY distancia_aprox ASC")
    LiveData<List<SucursalEntity>> getSucursalesCercanas(double latitud, double longitud);
    
    // ========== OPERACIONES ADMINISTRATIVAS ==========
    
    /**
     * Desactiva una sucursal (eliminación lógica)
     */
    @Query("UPDATE sucursales SET activo = 0, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int desactivarSucursal(long id, long fechaModificacion, String modificadoPor);
    
    /**
     * Activa una sucursal
     */
    @Query("UPDATE sucursales SET activo = 1, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int activarSucursal(long id, long fechaModificacion, String modificadoPor);
    
    /**
     * Actualiza la ubicación de una sucursal
     */
    @Query("UPDATE sucursales SET latitud = :latitud, longitud = :longitud, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int actualizarUbicacion(long id, double latitud, double longitud, long fechaModificacion, String modificadoPor);
    
    /**
     * Actualiza los horarios de una sucursal
     */
    @Query("UPDATE sucursales SET horario_apertura = :apertura, horario_cierre = :cierre, dias_operacion = :diasOperacion, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int actualizarHorarios(long id, String apertura, String cierre, String diasOperacion, long fechaModificacion, String modificadoPor);
    
    /**
     * Actualiza el gerente de una sucursal
     */
    @Query("UPDATE sucursales SET gerente = :gerente, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int actualizarGerente(long id, String gerente, long fechaModificacion, String modificadoPor);
    
    /**
     * Verifica si existe una sucursal con el mismo nombre en la misma ciudad
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE LOWER(nombre) = LOWER(:nombre) AND LOWER(ciudad) = LOWER(:ciudad) AND id != :excludeId")
    int existeSucursalEnCiudad(String nombre, String ciudad, long excludeId);
    
    /**
     * Verifica si existe una sucursal en las mismas coordenadas (con tolerancia)
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE " +
           "ABS(latitud - :latitud) < 0.001 AND ABS(longitud - :longitud) < 0.001 AND id != :excludeId")
    int existeSucursalEnUbicacion(double latitud, double longitud, long excludeId);
    
    // ========== CONTROL DE VERSIONES ==========
    
    /**
     * Obtiene la versión actual de una sucursal
     */
    @Query("SELECT version FROM sucursales WHERE id = :id")
    int getVersionSucursal(long id);
    
    /**
     * Actualiza una sucursal solo si la versión coincide (control de concurrencia optimista)
     */
    @Query("UPDATE sucursales SET nombre = :nombre, direccion = :direccion, ciudad = :ciudad, " +
           "telefono = :telefono, email = :email, latitud = :latitud, longitud = :longitud, " +
           "horario_apertura = :horarioApertura, horario_cierre = :horarioCierre, " +
           "dias_operacion = :diasOperacion, capacidad_maxima = :capacidadMaxima, " +
           "gerente = :gerente, descripcion = :descripcion, imagen_url = :imagenUrl, " +
           "servicios_disponibles = :serviciosDisponibles, fecha_modificacion = :fechaModificacion, " +
           "modificado_por = :modificadoPor, version = version + 1 " +
           "WHERE id = :id AND version = :versionEsperada")
    int updateSucursalConVersion(long id, String nombre, String direccion, String ciudad,
                                String telefono, String email, double latitud, double longitud,
                                String horarioApertura, String horarioCierre, String diasOperacion,
                                int capacidadMaxima, String gerente, String descripcion,
                                String imagenUrl, String serviciosDisponibles,
                                long fechaModificacion, String modificadoPor, int versionEsperada);
    
    // ========== ESTADÍSTICAS Y REPORTES ==========
    
    /**
     * Cuenta total de sucursales activas
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE activo = 1")
    LiveData<Integer> getCountSucursalesActivas();
    
    /**
     * Cuenta total de sucursales inactivas
     */
    @Query("SELECT COUNT(*) FROM sucursales WHERE activo = 0")
    LiveData<Integer> getCountSucursalesInactivas();
    
    /**
     * Obtiene sucursales creadas en un rango de fechas
     */
    @Query("SELECT * FROM sucursales WHERE fecha_creacion BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha_creacion DESC")
    LiveData<List<SucursalEntity>> getSucursalesPorFechaCreacion(long fechaInicio, long fechaFin);
    
    /**
     * Obtiene sucursales modificadas recientemente
     */
    @Query("SELECT * FROM sucursales WHERE fecha_modificacion > :fechaLimite ORDER BY fecha_modificacion DESC LIMIT :limite")
    LiveData<List<SucursalEntity>> getSucursalesModificadasRecientemente(long fechaLimite, int limite);
    
    /**
     * Obtiene la capacidad total de todas las sucursales activas
     */
    @Query("SELECT SUM(capacidad_maxima) FROM sucursales WHERE activo = 1")
    LiveData<Integer> getCapacidadTotalSucursales();
    
    /**
     * Obtiene sucursales por rango de capacidad
     */
    @Query("SELECT * FROM sucursales WHERE activo = 1 AND capacidad_maxima BETWEEN :capacidadMin AND :capacidadMax ORDER BY capacidad_maxima DESC")
    LiveData<List<SucursalEntity>> getSucursalesPorCapacidad(int capacidadMin, int capacidadMax);
    
    // ========== CONSULTAS DE HORARIOS ==========
    
    /**
     * Obtiene sucursales que operan en un día específico
     * Nota: Requiere que dias_operacion contenga el día en formato JSON
     */
    @Query("SELECT * FROM sucursales WHERE activo = 1 AND dias_operacion LIKE '%' || :dia || '%' ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesPorDia(String dia);
    
    /**
     * Obtiene sucursales abiertas en un horario específico
     */
    @Query("SELECT * FROM sucursales WHERE activo = 1 AND " +
           "CAST(REPLACE(horario_apertura, ':', '') AS INTEGER) <= :hora AND " +
           "CAST(REPLACE(horario_cierre, ':', '') AS INTEGER) >= :hora " +
           "ORDER BY nombre ASC")
    LiveData<List<SucursalEntity>> getSucursalesAbiertasEnHorario(int hora);
    
    // ========== OPERACIONES DE LIMPIEZA ==========
    
    /**
     * Elimina sucursales inactivas antiguas (limpieza de base de datos)
     */
    @Query("DELETE FROM sucursales WHERE activo = 0 AND fecha_modificacion < :fechaLimite")
    int eliminarSucursalesInactivasAntiguas(long fechaLimite);
    
    /**
     * Elimina todas las sucursales (usar solo en desarrollo/testing)
     */
    @Query("DELETE FROM sucursales")
    int eliminarTodasLasSucursales();
}