package com.example.cafefidelidaqrdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.cafefidelidaqrdemo.data.entity.VisitaEntity;
import java.util.List;

/**
 * DAO para operaciones de base de datos con visitas
 * Incluye consultas para manejo de sincronización offline
 */
@Dao
public interface VisitaAdminDao {
    
    // Operaciones básicas CRUD
    @Insert
    long insertar(VisitaEntity visita);
    
    @Insert
    List<Long> insertarTodas(List<VisitaEntity> visitas);
    
    @Update
    void actualizar(VisitaEntity visita);
    
    @Delete
    void eliminar(VisitaEntity visita);
    
    @Query("DELETE FROM visitas WHERE id = :id")
    void eliminarPorId(long id);
    
    @Query("DELETE FROM visitas")
    void eliminarTodas();
    
    // Consultas de búsqueda
    @Query("SELECT * FROM visitas WHERE id = :id")
    LiveData<VisitaEntity> obtenerPorId(long id);
    
    @Query("SELECT * FROM visitas WHERE id = :id")
    VisitaEntity obtenerPorIdSincrono(long id);
    
    @Query("SELECT * FROM visitas ORDER BY fecha_escaneo DESC")
    LiveData<List<VisitaEntity>> obtenerTodas();
    
    @Query("SELECT * FROM visitas ORDER BY fecha_escaneo DESC")
    List<VisitaEntity> obtenerTodasSincrono();
    
    @Query("SELECT * FROM visitas WHERE sucursal_id = :sucursalId ORDER BY fecha_escaneo DESC")
    LiveData<List<VisitaEntity>> obtenerPorSucursal(String sucursalId);
    
    @Query("SELECT * FROM visitas WHERE hash_qr = :hashQr LIMIT 1")
    VisitaEntity obtenerPorHashQr(String hashQr);
    
    // Consultas por estado de sincronización
    @Query("SELECT * FROM visitas WHERE estado_sincronizacion = :estado ORDER BY fecha_escaneo ASC")
    LiveData<List<VisitaEntity>> obtenerPorEstado(VisitaEntity.EstadoSincronizacion estado);
    
    @Query("SELECT * FROM visitas WHERE estado_sincronizacion = :estado ORDER BY fecha_escaneo ASC")
    List<VisitaEntity> obtenerPorEstadoSincrono(VisitaEntity.EstadoSincronizacion estado);
    
    @Query("SELECT * FROM visitas WHERE estado_sincronizacion = 'PENDIENTE' ORDER BY fecha_escaneo ASC")
    List<VisitaEntity> obtenerPendientesSincronizacion();
    
    @Query("SELECT * FROM visitas WHERE estado_sincronizacion = 'ERROR' AND intentos_sincronizacion < :maxIntentos ORDER BY fecha_escaneo ASC")
    List<VisitaEntity> obtenerConErrorParaReintentar(int maxIntentos);
    
    @Query("SELECT * FROM visitas WHERE estado_sincronizacion = 'ENVIADA' ORDER BY fecha_escaneo DESC LIMIT :limite")
    LiveData<List<VisitaEntity>> obtenerUltimasEnviadas(int limite);
    
    // Consultas de conteo
    @Query("SELECT COUNT(*) FROM visitas")
    LiveData<Integer> contarTodas();
    
    @Query("SELECT COUNT(*) FROM visitas WHERE estado_sincronizacion = 'PENDIENTE'")
    LiveData<Integer> contarPendientes();
    
    @Query("SELECT COUNT(*) FROM visitas WHERE estado_sincronizacion = 'ERROR'")
    LiveData<Integer> contarConError();
    
    @Query("SELECT COUNT(*) FROM visitas WHERE estado_sincronizacion = 'ENVIADA'")
    LiveData<Integer> contarEnviadas();
    
    @Query("SELECT COUNT(*) FROM visitas WHERE sucursal_id = :sucursalId AND estado_sincronizacion = 'ENVIADA'")
    LiveData<Integer> contarVisitasEnviadasPorSucursal(String sucursalId);
    
    // Consultas por fecha
    @Query("SELECT * FROM visitas WHERE fecha_escaneo >= :fechaInicio AND fecha_escaneo <= :fechaFin ORDER BY fecha_escaneo DESC")
    LiveData<List<VisitaEntity>> obtenerPorRangoFechas(long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM visitas WHERE DATE(fecha_escaneo/1000, 'unixepoch') = DATE('now') ORDER BY fecha_escaneo DESC")
    LiveData<List<VisitaEntity>> obtenerDeHoy();
    
    @Query("SELECT COUNT(*) FROM visitas WHERE DATE(fecha_escaneo/1000, 'unixepoch') = DATE('now') AND estado_sincronizacion = 'ENVIADA'")
    LiveData<Integer> contarVisitasHoyEnviadas();
    
    // Operaciones de sincronización
    @Query("UPDATE visitas SET estado_sincronizacion = 'ENVIADA', fecha_sincronizacion = :fechaSincronizacion, progreso_actualizado = :progreso, error_sincronizacion = NULL WHERE id = :id")
    void marcarComoEnviada(long id, long fechaSincronizacion, String progreso);
    
    @Query("UPDATE visitas SET estado_sincronizacion = 'ERROR', intentos_sincronizacion = intentos_sincronizacion + 1, error_sincronizacion = :error WHERE id = :id")
    void marcarComoError(long id, String error);
    
    @Query("UPDATE visitas SET estado_sincronizacion = 'PENDIENTE', error_sincronizacion = NULL WHERE id = :id")
    void reintentar(long id);
    
    @Query("UPDATE visitas SET estado_sincronizacion = 'PENDIENTE', error_sincronizacion = NULL WHERE estado_sincronizacion = 'ERROR' AND intentos_sincronizacion < :maxIntentos")
    void reintentarTodosLosErrores(int maxIntentos);
    
    // Limpieza de datos antiguos
    @Query("DELETE FROM visitas WHERE estado_sincronizacion = 'ENVIADA' AND fecha_sincronizacion < :fechaLimite")
    void eliminarAntiguasEnviadas(long fechaLimite);
    
    @Query("DELETE FROM visitas WHERE estado_sincronizacion = 'ERROR' AND intentos_sincronizacion >= :maxIntentos AND fecha_escaneo < :fechaLimite")
    void eliminarErroresAntiguos(int maxIntentos, long fechaLimite);
    
    // Validación de duplicados
    @Query("SELECT COUNT(*) FROM visitas WHERE hash_qr = :hashQr")
    int existeHashQr(String hashQr);
    
    @Query("SELECT COUNT(*) FROM visitas WHERE sucursal_id = :sucursalId AND DATE(fecha_escaneo/1000, 'unixepoch') = DATE('now')")
    int contarVisitasHoyEnSucursal(String sucursalId);
}