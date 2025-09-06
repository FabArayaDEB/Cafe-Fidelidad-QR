package com.example.cafefidelidaqrdemo.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;

import java.util.List;

@Dao
public interface BeneficioDao {

    @Query("SELECT * FROM beneficios ORDER BY lastSync DESC")
    LiveData<List<BeneficioEntity>> getAllBeneficios();

    @Query("SELECT * FROM beneficios WHERE estado = 'activo' ORDER BY lastSync DESC")
    LiveData<List<BeneficioEntity>> getBeneficiosActivos();

    @Query("SELECT * FROM beneficios WHERE id_beneficio = :id")
    LiveData<BeneficioEntity> getBeneficioById(String id);

    @Query("SELECT * FROM beneficios WHERE id_beneficio = :id")
    BeneficioEntity getBeneficioByIdSync(String id);

    @Query("SELECT * FROM beneficios WHERE estado = 'activo' AND " +
           "(vigencia_ini IS NULL OR vigencia_ini <= :fecha) AND " +
           "(vigencia_fin IS NULL OR vigencia_fin >= :fecha)")
    LiveData<List<BeneficioEntity>> getBeneficiosVigentes(long fecha);

    @Query("SELECT * FROM beneficios WHERE estado = 'activo' AND " +
           "(vigencia_ini IS NULL OR vigencia_ini <= :fecha) AND " +
           "(vigencia_fin IS NULL OR vigencia_fin >= :fecha)")
    List<BeneficioEntity> getBeneficiosVigentesSync(long fecha);

    @Query("SELECT * FROM beneficios WHERE " +
           "sucursales_aplicables LIKE '%' || :sucursalId || '%' AND " +
           "estado = 'activo' AND " +
           "(vigencia_ini IS NULL OR vigencia_ini <= :fecha) AND " +
           "(vigencia_fin IS NULL OR vigencia_fin >= :fecha)")
    LiveData<List<BeneficioEntity>> getBeneficiosPorSucursal(String sucursalId, long fecha);

    @Query("SELECT * FROM beneficios WHERE " +
           "sucursales_aplicables LIKE '%' || :sucursalId || '%' AND " +
           "estado = 'activo' AND " +
           "(vigencia_ini IS NULL OR vigencia_ini <= :fecha) AND " +
           "(vigencia_fin IS NULL OR vigencia_fin >= :fecha)")
    List<BeneficioEntity> getBeneficiosPorSucursalSync(String sucursalId, long fecha);

    @Query("SELECT COUNT(*) FROM beneficios WHERE " +
           "vigencia_ini <= :fechaFin AND vigencia_fin >= :fechaInicio AND " +
           "estado = 'activo' AND id_beneficio != :excludeId")
    int countBeneficiosConflictivos(long fechaInicio, long fechaFin, String excludeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBeneficio(BeneficioEntity beneficio);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBeneficios(List<BeneficioEntity> beneficios);

    @Update
    void updateBeneficio(BeneficioEntity beneficio);

    @Delete
    void deleteBeneficio(BeneficioEntity beneficio);

    @Query("DELETE FROM beneficios WHERE id_beneficio = :id")
    void deleteBeneficioById(String id);

    @Query("DELETE FROM beneficios")
    void deleteAllBeneficios();

    @Query("UPDATE beneficios SET estado = 'inactivo' WHERE id_beneficio = :id")
    void desactivarBeneficio(String id);

    @Query("UPDATE beneficios SET lastSync = :fecha WHERE id_beneficio = :id")
    void actualizarFechaModificacion(String id, long fecha);

    // Consultas para estadísticas y reportes
    @Query("SELECT COUNT(*) FROM beneficios WHERE estado = 'activo'")
    LiveData<Integer> getCountBeneficiosActivos();

    @Query("SELECT COUNT(*) FROM beneficios WHERE " +
           "estado = 'activo' AND " +
           "(vigencia_ini IS NULL OR vigencia_ini <= :fecha) AND " +
           "(vigencia_fin IS NULL OR vigencia_fin >= :fecha)")
    LiveData<Integer> getCountBeneficiosVigentes(long fecha);
    
    @Query("SELECT COUNT(*) FROM beneficios WHERE estado = 'inactivo'")
    LiveData<Integer> getCountBeneficiosInactivos();
    
    @Query("SELECT COUNT(*) FROM beneficios WHERE estado = 'activo'")
    int getCountBeneficiosActivosSync();
    
    @Query("SELECT COUNT(*) FROM beneficios WHERE estado = 'inactivo'")
    int getCountBeneficiosInactivosSync();

    @Query("SELECT * FROM beneficios WHERE " +
           "lastSync >= :fechaDesde AND lastSync <= :fechaHasta " +
           "ORDER BY lastSync DESC")
    LiveData<List<BeneficioEntity>> getBeneficiosPorPeriodo(long fechaDesde, long fechaHasta);
    
    @Query("SELECT COUNT(*) FROM beneficios WHERE " +
           "estado = 'activo' AND " +
           "producto_premio = CAST(:productoId AS TEXT)")
    int countBeneficiosActivosConProducto(long productoId);
}