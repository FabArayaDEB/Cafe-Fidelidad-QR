package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import java.util.List;

/**
 * DAO para operaciones CRUD de CanjeEntity
 */
@Dao
public interface CanjeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CanjeEntity canje);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CanjeEntity> canjes);
    
    @Update
    void update(CanjeEntity canje);
    
    @Delete
    void delete(CanjeEntity canje);
    
    @Query("SELECT * FROM canjes WHERE id_canje = :id")
    CanjeEntity getById(String id);
    
    @Query("SELECT * FROM canjes")
    List<CanjeEntity> getAll();
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente")
    List<CanjeEntity> getByCliente(String idCliente);
    
    @Query("SELECT * FROM canjes WHERE id_beneficio = :idBeneficio")
    List<CanjeEntity> getByBeneficio(String idBeneficio);
    
    @Query("SELECT * FROM canjes WHERE id_sucursal = :idSucursal")
    List<CanjeEntity> getBySucursal(String idSucursal);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND id_beneficio = :idBeneficio")
    List<CanjeEntity> getByClienteAndBeneficio(String idCliente, String idBeneficio);
    
    @Query("SELECT * FROM canjes WHERE estado_sync = :estadoSync")
    List<CanjeEntity> getByEstadoSync(String estadoSync);
    
    @Query("SELECT * FROM canjes WHERE estado_sync = 'PENDIENTE'")
    List<CanjeEntity> getPendientes();
    
    @Query("SELECT * FROM canjes WHERE estado_sync = 'ERROR'")
    List<CanjeEntity> getConError();
    
    @Query("SELECT * FROM canjes WHERE codigo_otp = :codigoOtp")
    CanjeEntity getByCodigo(String codigoOtp);
    
    @Query("SELECT * FROM canjes WHERE needsSync = 1")
    List<CanjeEntity> getPendientesSync();
    
    @Query("SELECT * FROM canjes WHERE synced = 0")
    List<CanjeEntity> getNoSincronizados();
    
    @Query("SELECT COUNT(*) FROM canjes")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_cliente = :idCliente")
    int getCountByCliente(String idCliente);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_beneficio = :idBeneficio")
    int getCountByBeneficio(String idBeneficio);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_sucursal = :idSucursal")
    int getCountBySucursal(String idSucursal);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE estado_sync = 'PENDIENTE'")
    int getCountPendientes();
    
    @Query("DELETE FROM canjes")
    void deleteAll();
    
    @Query("DELETE FROM canjes WHERE id_canje = :id")
    void deleteById(String id);
    
    @Query("DELETE FROM canjes WHERE id_cliente = :idCliente")
    void deleteByCliente(String idCliente);
    
    @Query("UPDATE canjes SET needsSync = 1 WHERE id_canje = :id")
    void markForSync(String id);
    
    @Query("UPDATE canjes SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id_canje = :id")
    void markAsSynced(String id, long timestamp);
    
    @Query("UPDATE canjes SET estado_sync = :nuevoEstado WHERE id_canje = :id")
    void updateEstadoSync(String id, String nuevoEstado);
    
    @Query("SELECT * FROM canjes WHERE fecha_hora BETWEEN :fechaInicio AND :fechaFin")
    List<CanjeEntity> getByRangoFecha(long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND fecha_hora BETWEEN :fechaInicio AND :fechaFin")
    List<CanjeEntity> getByClienteAndRangoFecha(String idCliente, long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente ORDER BY fecha_hora DESC LIMIT :limit")
    List<CanjeEntity> getUltimosCanjesCliente(String idCliente, int limit);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente ORDER BY fecha_hora DESC LIMIT 1")
    CanjeEntity getUltimoCanjeCliente(String idCliente);
    
    @Query("SELECT * FROM canjes ORDER BY fecha_hora DESC LIMIT :limit")
    List<CanjeEntity> getUltimosCanjes(int limit);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_cliente = :idCliente AND fecha_hora >= :fechaDesde")
    int getCountCanjesClienteDesde(String idCliente, long fechaDesde);
    
    @Query("SELECT * FROM canjes WHERE codigo_otp IS NOT NULL AND codigo_otp != ''")
    List<CanjeEntity> getConCodigoOTP();
    
    @Query("SELECT * FROM canjes WHERE fecha_hora >= :fechaHoy ORDER BY fecha_hora DESC")
    List<CanjeEntity> getCanjesHoy(long fechaHoy);
}