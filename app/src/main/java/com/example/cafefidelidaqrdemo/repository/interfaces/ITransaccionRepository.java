package com.example.cafefidelidaqrdemo.repository.interfaces;

import androidx.lifecycle.LiveData;
import com.example.cafefidelidaqrdemo.data.model.Transaccion;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.Date;
import java.util.List;

/**
 * Interfaz para el repositorio de transacciones
 * Define el contrato para operaciones CRUD y consultas de transacciones
 */
public interface ITransaccionRepository {
    
    // ==================== OBSERVABLES ====================
    
    /**
     * LiveData con todas las transacciones del usuario
     */
    LiveData<List<TransaccionEntity>> getTransaccionesByUsuario(Long idUsuario);
    
    /**
     * LiveData con historial de transacciones
     */
    LiveData<List<TransaccionEntity>> getHistorialTransacciones();
    
    /**
     * Estado de carga
     */
    LiveData<Boolean> getIsLoading();
    
    /**
     * Estado de error
     */
    LiveData<String> getError();
    
    /**
     * Estado offline
     */
    LiveData<Boolean> getIsOffline();
    
    // ==================== OPERACIONES CRUD ====================
    
    /**
     * Crea una nueva transacción
     */
    void createTransaccion(Transaccion transaccion, BaseRepository.RepositoryCallback<Transaccion> callback);
    
    /**
     * Obtiene una transacción por ID
     */
    void getTransaccionById(Long idTransaccion, BaseRepository.RepositoryCallback<Transaccion> callback);
    
    /**
     * Actualiza una transacción existente
     */
    void updateTransaccion(Transaccion transaccion, BaseRepository.SimpleCallback callback);
    
    /**
     * Elimina una transacción
     */
    void deleteTransaccion(Long idTransaccion, BaseRepository.SimpleCallback callback);
    
    // ==================== CONSULTAS ESPECÍFICAS ====================
    
    /**
     * Obtiene transacciones por rango de fechas
     */
    void getTransaccionesByDateRange(Long idUsuario, Date fechaInicio, Date fechaFin,
                                    BaseRepository.RepositoryCallback<List<Transaccion>> callback);
    
    /**
     * Obtiene transacciones por tipo
     */
    void getTransaccionesByTipo(Long idUsuario, String tipo,
                               BaseRepository.RepositoryCallback<List<Transaccion>> callback);
    
    /**
     * Obtiene transacciones pendientes de sincronización
     */
    void getTransaccionesPendientes(BaseRepository.RepositoryCallback<List<Transaccion>> callback);
    
    /**
     * Calcula el total de puntos por usuario
     */
    void getTotalPuntosByUsuario(Long idUsuario, BaseRepository.RepositoryCallback<Integer> callback);
    
    /**
     * Obtiene estadísticas de transacciones
     */
    void getEstadisticasTransacciones(Long idUsuario, BaseRepository.RepositoryCallback<EstadisticasTransacciones> callback);
    
    // ==================== SINCRONIZACIÓN ====================
    
    /**
     * Sincroniza transacciones con el servidor
     */
    void syncTransacciones(Long idUsuario, BaseRepository.SimpleCallback callback);
    
    /**
     * Fuerza la sincronización de todas las transacciones
     */
    void forceSyncAllTransacciones(BaseRepository.SimpleCallback callback);
    
    /**
     * Marca transacciones como sincronizadas
     */
    void markTransaccionesAsSynced(List<Long> transaccionIds, BaseRepository.SimpleCallback callback);
    
    // ==================== UTILIDADES ====================
    
    /**
     * Limpia errores
     */
    void clearError();
    
    /**
     * Limpia cache de transacciones
     */
    void clearCache(BaseRepository.SimpleCallback callback);
    
    /**
     * Limpia transacciones antiguas (más de X días)
     */
    void cleanOldTransacciones(int diasAntiguedad, BaseRepository.SimpleCallback callback);
    
    // ==================== CLASES AUXILIARES ====================
    
    /**
     * Clase para estadísticas de transacciones
     */
    class EstadisticasTransacciones {
        public int totalTransacciones;
        public int totalPuntos;
        public int puntosCanjeados;
        public int puntosDisponibles;
        public double promedioTransaccionMensual;
        public String mesConMasActividad;
        
        public EstadisticasTransacciones(int totalTransacciones, int totalPuntos, 
                                       int puntosCanjeados, int puntosDisponibles,
                                       double promedioTransaccionMensual, String mesConMasActividad) {
            this.totalTransacciones = totalTransacciones;
            this.totalPuntos = totalPuntos;
            this.puntosCanjeados = puntosCanjeados;
            this.puntosDisponibles = puntosDisponibles;
            this.promedioTransaccionMensual = promedioTransaccionMensual;
            this.mesConMasActividad = mesConMasActividad;
        }
    }
}