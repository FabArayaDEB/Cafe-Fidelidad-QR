package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import java.util.List;

/**
 * DAO para operaciones de Transacciones en cache offline
 */
@Dao
public interface TransaccionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaccion(TransaccionEntity transaccion);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransacciones(List<TransaccionEntity> transacciones);
    
    @Update
    void updateTransaccion(TransaccionEntity transaccion);
    
    @Query("SELECT * FROM transacciones WHERE id = :id LIMIT 1")
    TransaccionEntity getTransaccionById(String id);
    
    @Query("SELECT * FROM transacciones WHERE userId = :userId ORDER BY fecha DESC")
    List<TransaccionEntity> getTransaccionesByUserId(String userId);
    
    @Query("SELECT * FROM transacciones WHERE userId = :userId AND " +
           "fecha >= :fechaInicio AND fecha <= :fechaFin ORDER BY fecha DESC")
    List<TransaccionEntity> getTransaccionesByUserIdAndDateRange(String userId, 
                                                               long fechaInicio, 
                                                               long fechaFin);
    
    @Query("SELECT * FROM transacciones WHERE needsSync = 1 ORDER BY fecha ASC")
    List<TransaccionEntity> getTransaccionesNeedingSync();
    
    @Query("SELECT * FROM transacciones WHERE synced = 0 ORDER BY fecha ASC")
    List<TransaccionEntity> getTransaccionesNotSynced();
    
    @Query("UPDATE transacciones SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id = :id")
    void markAsSynced(String id, long timestamp);
    
    @Query("UPDATE transacciones SET needsSync = 1 WHERE id = :id")
    void markAsNeedingSync(String id);
    
    @Query("DELETE FROM transacciones WHERE id = :id")
    void deleteTransaccion(String id);
    
    @Query("DELETE FROM transacciones WHERE userId = :userId")
    void deleteTransaccionesByUserId(String userId);
    
    @Query("SELECT COUNT(*) FROM transacciones WHERE userId = :userId")
    int getTransaccionCountByUserId(String userId);
    
    @Query("SELECT SUM(monto) FROM transacciones WHERE userId = :userId AND synced = 1")
    Double getTotalMontoByUserId(String userId);
    
    @Query("SELECT SUM(puntos) FROM transacciones WHERE userId = :userId AND synced = 1")
    Integer getTotalPuntosByUserId(String userId);
    
    /**
     * Obtiene transacciones recientes para cache rápido (< 200ms)
     */
    @Query("SELECT * FROM transacciones WHERE userId = :userId AND " +
           "fecha >= :fechaLimite ORDER BY fecha DESC LIMIT :limit")
    List<TransaccionEntity> getTransaccionesRecientes(String userId, long fechaLimite, int limit);
    
    /**
     * Obtiene estadísticas rápidas del usuario
     */
    @Query("SELECT COUNT(*) as total, SUM(monto) as totalMonto, SUM(puntos) as totalPuntos " +
           "FROM transacciones WHERE userId = :userId AND synced = 1")
    EstadisticasTransaccion getEstadisticasUsuario(String userId);
    
    /**
     * Limpia transacciones antiguas para optimizar espacio
     */
    @Query("DELETE FROM transacciones WHERE fecha < :fechaLimite AND synced = 1")
    void limpiarTransaccionesAntiguas(long fechaLimite);
    
    /**
     * Elimina todas las transacciones de la tabla
     */
    @Query("DELETE FROM transacciones")
    void deleteAll();
    
    /**
     * Clase para estadísticas rápidas
     */
    class EstadisticasTransaccion {
        public int total;
        public double totalMonto;
        public int totalPuntos;
    }
}