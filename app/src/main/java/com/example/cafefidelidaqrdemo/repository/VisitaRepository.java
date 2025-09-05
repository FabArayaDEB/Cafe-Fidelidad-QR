package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import com.example.cafefidelidaqrdemo.models.Visita;
import com.example.cafefidelidaqrdemo.network.ApiService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio para gestión de visitas con sincronización offline/online
 */
public class VisitaRepository {
    private final VisitaDao visitaDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final MutableLiveData<List<VisitaEntity>> visitasLiveData;
    
    public VisitaRepository(Context context) {
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.visitaDao = database.visitaDao();
        this.apiService = ApiService.getInstance();
        this.executor = Executors.newFixedThreadPool(2);
        this.visitasLiveData = new MutableLiveData<>();
    }
    
    /**
     * Obtiene todas las visitas de un cliente
     */
    public LiveData<List<VisitaEntity>> getVisitasCliente(String idCliente) {
        executor.execute(() -> {
            List<VisitaEntity> visitas = visitaDao.getByCliente(idCliente);
            visitasLiveData.postValue(visitas);
        });
        return visitasLiveData;
    }
    
    /**
     * Obtiene el conteo de visitas de un cliente
     */
    public void getCountVisitasCliente(String idCliente, CountCallback callback) {
        executor.execute(() -> {
            int count = visitaDao.getCountByCliente(idCliente);
            callback.onResult(count);
        });
    }
    
    /**
     * Obtiene visitas de un cliente en un rango de fechas
     */
    public void getVisitasClienteRango(String idCliente, long fechaInicio, long fechaFin, VisitasCallback callback) {
        executor.execute(() -> {
            List<VisitaEntity> visitas = visitaDao.getByClienteAndRangoFecha(idCliente, fechaInicio, fechaFin);
            callback.onResult(visitas);
        });
    }
    
    /**
     * Obtiene las últimas visitas de un cliente
     */
    public void getUltimasVisitasCliente(String idCliente, int limit, VisitasCallback callback) {
        executor.execute(() -> {
            List<VisitaEntity> visitas = visitaDao.getUltimasVisitasCliente(idCliente, limit);
            callback.onResult(visitas);
        });
    }
    
    /**
     * Registra una nueva visita
     */
    public void registrarVisita(VisitaEntity visita, OperationCallback callback) {
        executor.execute(() -> {
            try {
                visitaDao.insert(visita);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    /**
     * Sincroniza visitas con el servidor
     */
    public void sincronizarVisitas(SyncCallback callback) {
        executor.execute(() -> {
            try {
                // Obtener visitas pendientes
                List<VisitaEntity> pendientes = visitaDao.getPendientesSync();
                
                int sincronizadas = 0;
                for (VisitaEntity visita : pendientes) {
                    try {
                        // Convertir a modelo para API
                        Visita visitaModel = convertToModel(visita);
                        
                        // Enviar a servidor
                        Visita resultado = apiService.createVisita(visitaModel);
                        
                        // Marcar como sincronizada
                        visitaDao.markAsSynced(visita.getId_visita(), System.currentTimeMillis());
                        sincronizadas++;
                        
                    } catch (Exception e) {
                        // Marcar como error
                        visitaDao.updateEstadoSync(visita.getId_visita(), "ERROR");
                    }
                }
                
                callback.onSyncComplete(sincronizadas, pendientes.size() - sincronizadas);
                
            } catch (Exception e) {
                callback.onSyncError(e.getMessage());
            }
        });
    }
    
    /**
     * Convierte VisitaEntity a Visita (modelo para API)
     */
    private Visita convertToModel(VisitaEntity entity) {
        Visita visita = new Visita();
        visita.setId(entity.getId_visita());
        visita.setUserId(entity.getId_cliente());
        visita.setSucursal(entity.getId_sucursal());
        visita.setFechaVisita(entity.getFecha_hora());
        visita.setQrCode(entity.getHash_qr());
        return visita;
    }
    
    /**
     * Convierte Visita a VisitaEntity
     */
    private VisitaEntity convertToEntity(Visita visita) {
        VisitaEntity entity = new VisitaEntity();
        entity.setId_visita(visita.getId());
        entity.setId_cliente(visita.getUserId());
        entity.setId_sucursal(visita.getSucursal());
        entity.setFecha_hora(visita.getFechaVisita());
        entity.setHash_qr(visita.getQrCode());
        entity.setOrigen("API");
        entity.setEstado_sync("ENVIADO");
        entity.setSynced(true);
        entity.setNeedsSync(false);
        return entity;
    }
    
    // Interfaces para callbacks
    public interface CountCallback {
        void onResult(int count);
    }
    
    public interface VisitasCallback {
        void onResult(List<VisitaEntity> visitas);
    }
    
    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface SyncCallback {
        void onSyncComplete(int sincronizadas, int errores);
        void onSyncError(String error);
    }
}