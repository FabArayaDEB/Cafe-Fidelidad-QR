package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import java.util.List;

/**
 * DAO para operaciones CRUD de VisitaEntity
 */
@Dao
public interface VisitaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VisitaEntity visita);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VisitaEntity> visitas);
    
    @Update
    void update(VisitaEntity visita);
    
    @Delete
    void delete(VisitaEntity visita);
    
    @Query("SELECT * FROM visitas WHERE id_visita = :id")
    VisitaEntity getById(String id);
    
    @Query("SELECT * FROM visitas")
    List<VisitaEntity> getAll();
    
    @Query("SELECT * FROM visitas WHERE id_cliente = :idCliente")
    List<VisitaEntity> getByCliente(String idCliente);
    
    @Query("SELECT * FROM visitas WHERE id_sucursal = :idSucursal")
    List<VisitaEntity> getBySucursal(String idSucursal);
    
    @Query("SELECT * FROM visitas WHERE id_cliente = :idCliente AND id_sucursal = :idSucursal")
    List<VisitaEntity> getByClienteAndSucursal(String idCliente, String idSucursal);
    
    @Query("SELECT * FROM visitas WHERE estado_sync = :estadoSync")
    List<VisitaEntity> getByEstadoSync(String estadoSync);
    
    @Query("SELECT * FROM visitas WHERE estado_sync = 'PENDIENTE'")
    List<VisitaEntity> getPendientes();
    
    @Query("SELECT * FROM visitas WHERE estado_sync = 'ERROR'")
    List<VisitaEntity> getConError();
    
    @Query("SELECT * FROM visitas WHERE origen = :origen")
    List<VisitaEntity> getByOrigen(String origen);
    
    @Query("SELECT * FROM visitas WHERE hash_qr = :hashQr")
    List<VisitaEntity> getByHashQR(String hashQr);
    
    @Query("SELECT * FROM visitas WHERE needsSync = 1")
    List<VisitaEntity> getPendientesSync();
    
    @Query("SELECT * FROM visitas WHERE synced = 0")
    List<VisitaEntity> getNoSincronizadas();
    
    @Query("SELECT COUNT(*) FROM visitas")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM visitas WHERE id_cliente = :idCliente")
    int getCountByCliente(String idCliente);
    
    @Query("SELECT COUNT(*) FROM visitas WHERE id_sucursal = :idSucursal")
    int getCountBySucursal(String idSucursal);
    
    @Query("SELECT COUNT(*) FROM visitas WHERE estado_sync = 'PENDIENTE'")
    int getCountPendientes();
    
    @Query("DELETE FROM visitas")
    void deleteAll();
    
    @Query("DELETE FROM visitas WHERE id_visita = :id")
    void deleteById(String id);
    
    @Query("DELETE FROM visitas WHERE id_cliente = :idCliente")
    void deleteByCliente(String idCliente);
    
    @Query("UPDATE visitas SET needsSync = 1 WHERE id_visita = :id")
    void markForSync(String id);
    
    @Query("UPDATE visitas SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id_visita = :id")
    void markAsSynced(String id, long timestamp);
    
    @Query("UPDATE visitas SET estado_sync = :nuevoEstado WHERE id_visita = :id")
    void updateEstadoSync(String id, String nuevoEstado);
    
    @Query("SELECT * FROM visitas WHERE fecha_hora BETWEEN :fechaInicio AND :fechaFin")
    List<VisitaEntity> getByRangoFecha(long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM visitas WHERE id_cliente = :idCliente AND fecha_hora BETWEEN :fechaInicio AND :fechaFin")
    List<VisitaEntity> getByClienteAndRangoFecha(String idCliente, long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM visitas WHERE id_cliente = :idCliente ORDER BY fecha_hora DESC LIMIT :limit")
    List<VisitaEntity> getUltimasVisitasCliente(String idCliente, int limit);
    
    @Query("SELECT * FROM visitas WHERE id_cliente = :idCliente ORDER BY fecha_hora DESC LIMIT 1")
    VisitaEntity getUltimaVisitaCliente(String idCliente);
    
    @Query("SELECT * FROM visitas ORDER BY fecha_hora DESC LIMIT :limit")
    List<VisitaEntity> getUltimasVisitas(int limit);
    
    @Query("SELECT COUNT(*) FROM visitas WHERE id_cliente = :idCliente AND fecha_hora >= :fechaDesde")
    int getCountVisitasClienteDesde(String idCliente, long fechaDesde);
}