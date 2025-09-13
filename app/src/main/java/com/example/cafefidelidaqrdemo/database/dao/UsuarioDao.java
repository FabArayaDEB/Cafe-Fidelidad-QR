package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;
import java.util.List;

/**
 * DAO para operaciones de Usuario en cache offline
 */
@Dao
public interface UsuarioDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsuario(UsuarioEntity usuario);
    
    @Update
    void updateUsuario(UsuarioEntity usuario);
    
    @Query("SELECT * FROM usuarios WHERE uid = :uid LIMIT 1")
    UsuarioEntity getUsuarioById(String uid);
    
    @Query("SELECT * FROM usuarios WHERE uid = :uid LIMIT 1")
    UsuarioEntity getUsuarioByIdSync(String uid);
    
    @Query("SELECT * FROM usuarios WHERE needsSync = 1")
    List<UsuarioEntity> getUsuariosNeedingSync();
    
    @Query("UPDATE usuarios SET puntos = :puntos, totalCompras = :totalCompras, " +
           "nivel = :nivel, ultimaVisita = :ultimaVisita, needsSync = 1 WHERE uid = :uid")
    void updatePuntosYCompras(String uid, int puntos, double totalCompras, 
                             String nivel, long ultimaVisita);
    
    @Query("UPDATE usuarios SET needsSync = 0, lastSync = :timestamp WHERE uid = :uid")
    void markAsSynced(String uid, long timestamp);
    
    @Query("UPDATE usuarios SET needsSync = 1 WHERE uid = :uid")
    void markAsNeedingSync(String uid);
    
    @Query("DELETE FROM usuarios WHERE uid = :uid")
    void deleteUsuario(String uid);
    
    @Query("SELECT COUNT(*) FROM usuarios")
    int getUsuarioCount();
    
    @Query("SELECT * FROM usuarios ORDER BY lastSync DESC LIMIT 1")
    UsuarioEntity getLastSyncedUsuario();
    
    /**
     * Obtiene usuario con cache rápido (< 200ms según requisitos)
     */
    @Query("SELECT * FROM usuarios WHERE uid = :uid AND " +
           "(lastSync > :cacheValidTime OR needsSync = 0) LIMIT 1")
    UsuarioEntity getUsuarioFromCache(String uid, long cacheValidTime);
    
    /**
     * Obtiene todos los usuarios
     */
    @Query("SELECT * FROM usuarios")
    List<UsuarioEntity> getAllUsuarios();
    
    /**
     * Actualiza solo los campos modificables del perfil
     */
    @Query("UPDATE usuarios SET names = :names, telefono = :telefono, " +
           "fechaNacimiento = :fechaNacimiento, needsSync = 1 WHERE uid = :uid")
    void updatePerfilUsuario(String uid, String names, String telefono, String fechaNacimiento);
    
    /**
     * Elimina todos los usuarios de la tabla
     */
    @Query("DELETE FROM usuarios")
    void deleteAll();
}