package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import java.util.List;

/**
 * DAO para operaciones CRUD de ClienteEntity
 */
@Dao
public interface ClienteDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClienteEntity cliente);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ClienteEntity> clientes);
    
    @Update
    void update(ClienteEntity cliente);
    
    @Delete
    void delete(ClienteEntity cliente);
    
    @Query("SELECT * FROM clientes WHERE id_cliente = :id")
    ClienteEntity getById(String id);
    
    @Query("SELECT * FROM clientes WHERE email = :email LIMIT 1")
    ClienteEntity getByEmail(String email);
    
    @Query("SELECT * FROM clientes")
    List<ClienteEntity> getAll();
    
    @Query("SELECT * FROM clientes WHERE estado = :estado")
    List<ClienteEntity> getByEstado(String estado);
    
    @Query("SELECT * FROM clientes WHERE estado = 'activo'")
    List<ClienteEntity> getActivos();
    
    @Query("SELECT * FROM clientes WHERE needsSync = 1")
    List<ClienteEntity> getPendientesSync();
    
    @Query("SELECT * FROM clientes WHERE synced = 0")
    List<ClienteEntity> getNoSincronizados();
    
    @Query("SELECT COUNT(*) FROM clientes")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM clientes WHERE estado = 'activo'")
    int getCountActivos();
    
    @Query("DELETE FROM clientes")
    void deleteAll();
    
    @Query("DELETE FROM clientes WHERE id_cliente = :id")
    void deleteById(String id);
    
    @Query("UPDATE clientes SET needsSync = 1 WHERE id_cliente = :id")
    void markForSync(String id);
    
    @Query("UPDATE clientes SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id_cliente = :id")
    void markAsSynced(String id, long timestamp);
    
    @Query("SELECT * FROM clientes WHERE nombre LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%'")
    List<ClienteEntity> search(String query);
    
    @Query("SELECT * FROM clientes WHERE fecha_nac BETWEEN :fechaInicio AND :fechaFin")
    List<ClienteEntity> getByRangoFechaNacimiento(long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM clientes WHERE creado_en >= :fecha")
    List<ClienteEntity> getRegistradosDespuesDe(long fecha);
    
    @Query("SELECT * FROM clientes ORDER BY creado_en DESC LIMIT :limit")
    List<ClienteEntity> getUltimosRegistrados(int limit);
}