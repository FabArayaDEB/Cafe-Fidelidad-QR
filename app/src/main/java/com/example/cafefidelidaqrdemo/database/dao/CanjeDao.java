package com.example.cafefidelidaqrdemo.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;

import java.util.List;

/**
 * DAO para operaciones CRUD de CanjeEntity
 * Maneja los canjes de beneficios con OTP y temporizador
 */
@Dao
public interface CanjeDao {
    
    // ========== OPERACIONES BÁSICAS CRUD ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CanjeEntity canje);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CanjeEntity> canjes);
    
    @Update
    void update(CanjeEntity canje);
    
    @Delete
    void delete(CanjeEntity canje);
    
    @Query("DELETE FROM canjes WHERE id_canje = :idCanje")
    void deleteById(String idCanje);
    
    @Query("DELETE FROM canjes")
    void deleteAll();
    
    // ========== CONSULTAS DE BÚSQUEDA ==========
    
    @Query("SELECT * FROM canjes WHERE id_canje = :idCanje LIMIT 1")
    CanjeEntity getById(String idCanje);
    
    @Query("SELECT * FROM canjes WHERE id_canje = :idCanje LIMIT 1")
    LiveData<CanjeEntity> getByIdLive(String idCanje);
    
    @Query("SELECT * FROM canjes WHERE otp_codigo = :otpCodigo AND otp_usado = 0 LIMIT 1")
    CanjeEntity getByOtpActivo(String otpCodigo);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente ORDER BY fecha_solicitud DESC")
    LiveData<List<CanjeEntity>> getCanjesByCliente(String idCliente);
    
    @Query("SELECT * FROM canjes WHERE id_beneficio = :idBeneficio ORDER BY fecha_solicitud DESC")
    LiveData<List<CanjeEntity>> getCanjesByBeneficio(String idBeneficio);
    
    @Query("SELECT * FROM canjes WHERE id_sucursal = :idSucursal ORDER BY fecha_solicitud DESC")
    LiveData<List<CanjeEntity>> getCanjesBySucursal(String idSucursal);
    
    // ========== CONSULTAS POR ESTADO ==========
    
    @Query("SELECT * FROM canjes WHERE estado = :estado ORDER BY fecha_solicitud DESC")
    LiveData<List<CanjeEntity>> getCanjesByEstado(String estado);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND estado = :estado ORDER BY fecha_solicitud DESC")
    LiveData<List<CanjeEntity>> getCanjesByClienteYEstado(String idCliente, String estado);
    
    @Query("SELECT * FROM canjes WHERE estado = 'PENDIENTE' AND otp_expiracion > :tiempoActual")
    List<CanjeEntity> getCanjesPendientesValidos(long tiempoActual);
    
    @Query("SELECT * FROM canjes WHERE estado = 'PENDIENTE' AND otp_expiracion <= :tiempoActual")
    List<CanjeEntity> getCanjesToExpirar(long tiempoActual);
    
    // ========== CONSULTAS DE OTP ==========
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND estado = 'PENDIENTE' AND otp_expiracion > :tiempoActual LIMIT 1")
    CanjeEntity getOtpActivoCliente(String idCliente, long tiempoActual);
    
    @Query("UPDATE canjes SET otp_usado = 1, estado = 'CANJEADO', fecha_canje = :fechaCanje, cajero_id = :cajeroId WHERE otp_codigo = :otpCodigo AND otp_usado = 0")
    int marcarOtpUsado(String otpCodigo, long fechaCanje, String cajeroId);
    
    @Query("UPDATE canjes SET estado = 'EXPIRADO' WHERE estado = 'PENDIENTE' AND otp_expiracion <= :tiempoActual")
    int expirarOtpsVencidos(long tiempoActual);
    
    // ========== CONSULTAS DE SINCRONIZACIÓN ==========
    
    @Query("SELECT * FROM canjes WHERE needsSync = 1")
    List<CanjeEntity> getCanjesParaSincronizar();
    
    @Query("SELECT * FROM canjes WHERE synced = 0")
    List<CanjeEntity> getCanjesNoSincronizados();
    
    @Query("UPDATE canjes SET needsSync = 1, synced = 0 WHERE id_canje = :idCanje")
    void marcarParaSincronizar(String idCanje);
    
    @Query("UPDATE canjes SET needsSync = 0, synced = 1, lastSync = :lastSync WHERE id_canje = :idCanje")
    void marcarSincronizado(String idCanje, long lastSync);
    
    // ========== CONSULTAS DE ESTADÍSTICAS ==========
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_cliente = :idCliente")
    int getConteoCanjesCliente(String idCliente);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_cliente = :idCliente AND estado = 'CANJEADO'")
    int getConteoCanjesCompletadosCliente(String idCliente);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_beneficio = :idBeneficio AND estado = 'CANJEADO'")
    int getConteoCanjesBeneficio(String idBeneficio);
    
    @Query("SELECT COUNT(*) FROM canjes WHERE fecha_solicitud >= :fechaInicio AND fecha_solicitud <= :fechaFin")
    int getConteoCanjesEnRango(long fechaInicio, long fechaFin);
    
    // ========== CONSULTAS DE VALIDACIÓN ==========
    
    @Query("SELECT COUNT(*) FROM canjes WHERE id_cliente = :idCliente AND id_beneficio = :idBeneficio AND estado IN ('PENDIENTE', 'CANJEADO')")
    int verificarCanjeExistente(String idCliente, String idBeneficio);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND id_beneficio = :idBeneficio AND estado = 'CANJEADO' ORDER BY fecha_canje DESC LIMIT 1")
    CanjeEntity getUltimoCanjeCompletado(String idCliente, String idBeneficio);
    
    // ========== CONSULTAS DE LIMPIEZA ==========
    
    @Query("DELETE FROM canjes WHERE estado = 'EXPIRADO' AND fecha_solicitud < :fechaLimite")
    int limpiarCanjesToExpirados(long fechaLimite);
    
    @Query("DELETE FROM canjes WHERE estado = 'CANCELADO' AND fecha_solicitud < :fechaLimite")
    int limpiarCanjesCancelados(long fechaLimite);
    
    // ========== CONSULTAS PARA FRAGMENTO MIS BENEFICIOS ==========
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND estado = 'CANJEADO' ORDER BY fecha_canje DESC")
    LiveData<List<CanjeEntity>> getHistorialCanjesCliente(String idCliente);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente AND fecha_solicitud >= :fechaInicio AND fecha_solicitud <= :fechaFin ORDER BY fecha_solicitud DESC")
    LiveData<List<CanjeEntity>> getCanjesClienteEnRango(String idCliente, long fechaInicio, long fechaFin);
    
    @Query("SELECT * FROM canjes WHERE id_cliente = :idCliente ORDER BY fecha_solicitud DESC LIMIT :limite")
    LiveData<List<CanjeEntity>> getUltimosCanjesCliente(String idCliente, int limite);
}