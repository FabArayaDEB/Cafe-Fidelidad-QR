package com.example.cafefidelidaqrdemo.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cafefidelidaqrdemo.database.entities.UbicacionEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface UbicacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUbicacion(UbicacionEntity ubicacion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertUbicaciones(List<UbicacionEntity> ubicaciones);

    @Update
    int updateUbicacion(UbicacionEntity ubicacion);

    @Delete
    int deleteUbicacion(UbicacionEntity ubicacion);

    @Query("SELECT * FROM ubicaciones WHERE id = :id")
    LiveData<UbicacionEntity> getUbicacionById(int id);

    @Query("SELECT * FROM ubicaciones WHERE id = :id")
    UbicacionEntity getUbicacionByIdSync(int id);

    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId ORDER BY fecha_registro DESC")
    LiveData<List<UbicacionEntity>> getUbicacionesByUsuario(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId ORDER BY fecha_registro DESC")
    List<UbicacionEntity> getUbicacionesByUsuarioSync(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId ORDER BY fecha_registro DESC LIMIT 1")
    LiveData<UbicacionEntity> getUltimaUbicacionUsuario(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId ORDER BY fecha_registro DESC LIMIT 1")
    UbicacionEntity getUltimaUbicacionUsuarioSync(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE es_sucursal_cercana = 1 AND usuario_id = :usuarioId ORDER BY fecha_registro DESC")
    LiveData<List<UbicacionEntity>> getUbicacionesCercanasSucursales(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE sincronizado = 0 ORDER BY fecha_registro ASC")
    List<UbicacionEntity> getUbicacionesNoSincronizadas();

    @Query("SELECT * FROM ubicaciones WHERE fecha_registro BETWEEN :fechaInicio AND :fechaFin AND usuario_id = :usuarioId ORDER BY fecha_registro DESC")
    LiveData<List<UbicacionEntity>> getUbicacionesPorRangoFecha(int usuarioId, Date fechaInicio, Date fechaFin);

    @Query("SELECT * FROM ubicaciones WHERE fecha_registro BETWEEN :fechaInicio AND :fechaFin AND usuario_id = :usuarioId ORDER BY fecha_registro DESC")
    List<UbicacionEntity> getUbicacionesPorRangoFechaSync(int usuarioId, Date fechaInicio, Date fechaFin);

    @Query("SELECT * FROM ubicaciones WHERE sucursal_id = :sucursalId AND usuario_id = :usuarioId ORDER BY fecha_registro DESC")
    LiveData<List<UbicacionEntity>> getUbicacionesPorSucursal(int usuarioId, int sucursalId);

    @Query("SELECT COUNT(*) FROM ubicaciones WHERE usuario_id = :usuarioId")
    LiveData<Integer> getConteoUbicacionesUsuario(int usuarioId);

    @Query("SELECT COUNT(*) FROM ubicaciones WHERE usuario_id = :usuarioId AND es_sucursal_cercana = 1")
    LiveData<Integer> getConteoVisitasSucursales(int usuarioId);

    @Query("SELECT COUNT(*) FROM ubicaciones WHERE sincronizado = 0")
    int getConteoUbicacionesNoSincronizadas();

    @Query("UPDATE ubicaciones SET sincronizado = 1 WHERE id IN (:ids)")
    int marcarComoSincronizadas(List<Integer> ids);

    @Query("UPDATE ubicaciones SET sincronizado = 1 WHERE id = :id")
    int marcarComoSincronizada(int id);

    @Query("UPDATE ubicaciones SET es_sucursal_cercana = :esCercana, sucursal_id = :sucursalId, distancia_sucursal = :distancia WHERE id = :id")
    int actualizarInfoSucursal(int id, boolean esCercana, Integer sucursalId, Float distancia);

    @Query("UPDATE ubicaciones SET direccion = :direccion, ciudad = :ciudad WHERE id = :id")
    int actualizarDireccion(int id, String direccion, String ciudad);

    @Query("DELETE FROM ubicaciones WHERE usuario_id = :usuarioId")
    int deleteUbicacionesByUsuario(int usuarioId);

    @Query("DELETE FROM ubicaciones WHERE fecha_registro < :fechaLimite")
    int deleteUbicacionesAntiguas(Date fechaLimite);

    @Query("DELETE FROM ubicaciones WHERE sincronizado = 1 AND fecha_registro < :fechaLimite")
    int deleteUbicacionesSincronizadasAntiguas(Date fechaLimite);

    // Consultas para análisis y reportes
    @Query("SELECT DISTINCT ciudad FROM ubicaciones WHERE usuario_id = :usuarioId AND ciudad IS NOT NULL ORDER BY ciudad")
    LiveData<List<String>> getCiudadesVisitadas(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId AND latitud BETWEEN :latMin AND :latMax AND longitud BETWEEN :lngMin AND :lngMax ORDER BY fecha_registro DESC")
    LiveData<List<UbicacionEntity>> getUbicacionesEnArea(int usuarioId, double latMin, double latMax, double lngMin, double lngMax);

    @Query("SELECT AVG(precision) FROM ubicaciones WHERE usuario_id = :usuarioId")
    LiveData<Float> getPrecisionPromedio(int usuarioId);

    // Consulta para obtener ubicaciones recientes (últimas 24 horas)
    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId AND fecha_registro > datetime('now', '-1 day') ORDER BY fecha_registro DESC")
    LiveData<List<UbicacionEntity>> getUbicacionesRecientes(int usuarioId);

    @Query("SELECT * FROM ubicaciones WHERE usuario_id = :usuarioId AND fecha_registro > datetime('now', '-1 day') ORDER BY fecha_registro DESC")
    List<UbicacionEntity> getUbicacionesRecientesSync(int usuarioId);
}