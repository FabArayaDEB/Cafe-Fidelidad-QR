package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.ReglaEntity;
import java.util.List;

/**
 * DAO para operaciones CRUD de ReglaEntity
 */
@Dao
public interface ReglaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReglaEntity regla);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReglaEntity> reglas);
    
    @Update
    void update(ReglaEntity regla);
    
    @Delete
    void delete(ReglaEntity regla);
    
    @Query("SELECT * FROM reglas WHERE id_regla = :id")
    ReglaEntity getById(String id);
    
    @Query("SELECT * FROM reglas")
    List<ReglaEntity> getAll();
    
    @Query("SELECT * FROM reglas WHERE needsSync = 1")
    List<ReglaEntity> getPendientesSync();
    
    @Query("SELECT * FROM reglas WHERE synced = 0")
    List<ReglaEntity> getNoSincronizadas();
    
    @Query("SELECT COUNT(*) FROM reglas")
    int getCount();
    
    @Query("DELETE FROM reglas")
    void deleteAll();
    
    @Query("DELETE FROM reglas WHERE id_regla = :id")
    void deleteById(String id);
    
    @Query("UPDATE reglas SET needsSync = 1 WHERE id_regla = :id")
    void markForSync(String id);
    
    @Query("UPDATE reglas SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id_regla = :id")
    void markAsSynced(String id, long timestamp);
    
    @Query("SELECT * FROM reglas WHERE descripcion LIKE '%' || :query || '%' OR expresion LIKE '%' || :query || '%'")
    List<ReglaEntity> search(String query);
    
    @Query("SELECT * FROM reglas WHERE descripcion LIKE '%' || :palabraClave || '%'")
    List<ReglaEntity> getByDescripcionContiene(String palabraClave);
    
    @Query("SELECT * FROM reglas WHERE expresion LIKE '%' || :palabraClave || '%'")
    List<ReglaEntity> getByExpresionContiene(String palabraClave);
    
    @Query("SELECT * FROM reglas WHERE expresion LIKE '{%}' OR expresion LIKE '[%]'")
    List<ReglaEntity> getReglasJSON();
    
    @Query("SELECT * FROM reglas WHERE expresion IS NOT NULL AND expresion != ''")
    List<ReglaEntity> getConExpresionValida();
    
    @Query("SELECT * FROM reglas ORDER BY descripcion ASC")
    List<ReglaEntity> getAllOrdenadas();
}