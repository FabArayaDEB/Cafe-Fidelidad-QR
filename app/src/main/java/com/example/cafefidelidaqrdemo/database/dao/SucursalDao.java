package com.example.cafefidelidaqrdemo.database.dao;

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
 * DAO para operaciones CRUD de SucursalEntity
 */
@Dao
public interface SucursalDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SucursalEntity sucursal);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSucursal(SucursalEntity sucursal);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SucursalEntity> sucursales);
    
    @Update
    void update(SucursalEntity sucursal);
    
    @Delete
    void delete(SucursalEntity sucursal);
    
    @Delete
    void deleteSucursal(SucursalEntity sucursal);
    
    @Query("SELECT * FROM sucursales WHERE id_sucursal = :id")
    SucursalEntity getById(String id);
    
    @Query("SELECT * FROM sucursales WHERE id_sucursal = :id")
    LiveData<SucursalEntity> getSucursalById(String id);
    
    @Query("SELECT * FROM sucursales")
    List<SucursalEntity> getAll();
    
    @Query("SELECT * FROM sucursales")
    LiveData<List<SucursalEntity>> getAllSucursales();
    
    @Query("SELECT * FROM sucursales")
    List<SucursalEntity> getAllSucursalesSync();
    
    @Query("SELECT * FROM sucursales WHERE estado = :estado")
    List<SucursalEntity> getByEstado(String estado);
    
    @Query("SELECT * FROM sucursales WHERE estado = 'activa'")
    List<SucursalEntity> getActivas();
    
    @Query("SELECT * FROM sucursales WHERE estado = 'activa'")
    LiveData<List<SucursalEntity>> getSucursalesActivas();
    
    @Query("SELECT * FROM sucursales WHERE needsSync = 1")
    List<SucursalEntity> getPendientesSync();
    
    @Query("SELECT * FROM sucursales WHERE synced = 0")
    List<SucursalEntity> getNoSincronizadas();
    
    @Query("SELECT COUNT(*) FROM sucursales")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE estado = 'activa'")
    int getCountActivas();
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE estado = 'activa'")
    LiveData<Integer> getCountSucursalesActivas();
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE estado = 'inactiva'")
    LiveData<Integer> getCountSucursalesInactivas();
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE estado = 'activa'")
    int getCountSucursalesActivasSync();
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE estado = 'inactiva'")
    int getCountSucursalesInactivasSync();
    
    @Query("DELETE FROM sucursales")
    void deleteAll();
    
    @Query("DELETE FROM sucursales WHERE id_sucursal = :id")
    void deleteById(String id);
    
    @Query("UPDATE sucursales SET needsSync = 1 WHERE id_sucursal = :id")
    void markForSync(String id);
    
    @Query("UPDATE sucursales SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id_sucursal = :id")
    void markAsSynced(String id, long timestamp);
    
    @Query("SELECT * FROM sucursales WHERE nombre LIKE '%' || :query || '%' OR direccion LIKE '%' || :query || '%' OR horario LIKE '%' || :query || '%'")
    List<SucursalEntity> searchSucursales(String query);
    
    @Query("SELECT * FROM sucursales WHERE lat BETWEEN :latMin AND :latMax AND lon BETWEEN :lonMin AND :lonMax")
    List<SucursalEntity> getByRangoCoordenadas(double latMin, double latMax, double lonMin, double lonMax);
    
    @Query("SELECT * FROM sucursales WHERE estado = 'activa' ORDER BY nombre ASC")
    List<SucursalEntity> getActivasOrdenadas();
    
    @Query("SELECT * FROM sucursales WHERE nombre LIKE '%' || :nombre || '%'")
    LiveData<List<SucursalEntity>> buscarSucursalesPorNombre(String nombre);
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE lat = :lat AND lon = :lon AND id_sucursal != :excludeId")
    int existeSucursalEnUbicacion(double lat, double lon, String excludeId);
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE nombre = :nombre AND direccion = :direccion AND id_sucursal != :excludeId")
    int existeSucursalEnCiudad(String nombre, String direccion, String excludeId);
    
    @Query("SELECT version FROM sucursales WHERE id_sucursal = :id")
    int getVersionSucursal(String id);
    
    @Update
    int updateSucursal(SucursalEntity sucursal);
    
    @Query("UPDATE sucursales SET estado = 'activa' WHERE id_sucursal = :sucursalId")
    void activarSucursal(long sucursalId);
    
    @Query("UPDATE sucursales SET estado = 'inactiva' WHERE id_sucursal = :sucursalId")
    void desactivarSucursal(long sucursalId);
    
    @Query("UPDATE sucursales SET estado = 'eliminada' WHERE id_sucursal = :sucursalId")
    void eliminarSucursal(long sucursalId);
    
    @Query("SELECT COUNT(*) FROM sucursales WHERE nombre = :nombre AND id_sucursal != :idExcluir")
    int existeSucursalPorNombreExcluyendoId(String nombre, String idExcluir);
    
}