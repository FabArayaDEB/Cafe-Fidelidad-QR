package com.example.cafefidelidaqrdemo.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import com.example.cafefidelidaqrdemo.models.Visita;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import java.util.List;

/**
 * Worker para sincronización de visitas en segundo plano
 */
public class VisitaSyncWorker extends Worker {
    
    private final VisitaDao visitaDao;
    private final ApiService apiService;
    
    public VisitaSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.visitaDao = database.visitaDao();
        this.apiService = ApiService.getInstance();
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            // Verificar conectividad
            if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                return Result.retry();
            }
            
            // Obtener visitas pendientes de sincronización
            List<VisitaEntity> pendingVisitas = visitaDao.getPendientesSync();
            
            if (pendingVisitas.isEmpty()) {
                return Result.success();
            }
            
            int successCount = 0;
            int totalCount = pendingVisitas.size();
            
            for (VisitaEntity visita : pendingVisitas) {
                try {
                    // Convertir a modelo para API
                    Visita visitaModel = convertToModel(visita);
                    
                    // Enviar a API
                    Visita syncedVisita = apiService.createVisita(visitaModel);
                    
                    // Actualizar con ID del servidor si es necesario
                    if (syncedVisita.getId() != null && !syncedVisita.getId().equals(visita.getId_visita())) {
                        visita.setId_visita(syncedVisita.getId());
                    }
                    
                    // Actualizar estado de sincronización
                    visita.setEstado_sync("ENVIADO");
                    visita.setLastSync(System.currentTimeMillis());
                    
                    // Actualizar en base de datos local
                    visitaDao.update(visita);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    // Log del error pero continuar con otras visitas
                    android.util.Log.e("VisitaSyncWorker", "Error sincronizando visita " + visita.getId_visita(), e);
                    
                    // Marcar como error si es un error permanente
                    if (isPermanentError(e)) {
                        visita.setEstado_sync("ERROR");
                        visitaDao.update(visita);
                    }
                }
            }
            
            // Determinar resultado basado en éxito
            if (successCount == totalCount) {
                return Result.success();
            } else if (successCount > 0) {
                // Sincronización parcial - reintentar los fallidos
                return Result.retry();
            } else {
                // Falló completamente - reintentar más tarde
                return Result.retry();
            }
            
        } catch (Exception e) {
            android.util.Log.e("VisitaSyncWorker", "Error general en sincronización de visitas", e);
            return Result.retry();
        }
    }
    
    /**
     * Convierte VisitaEntity a Visita (modelo para API)
     */
    private Visita convertToModel(VisitaEntity entity) {
        Visita visita = new Visita();
        visita.setId(entity.getId_visita());
        visita.setClienteId(entity.getId_cliente());
        visita.setSucursalId(entity.getId_sucursal());
        visita.setFechaHora(entity.getFecha_hora());
        visita.setOrigen(entity.getOrigen());
        visita.setHashQr(entity.getHash_qr());
        return visita;
    }
    
    /**
     * Determina si un error es permanente (no debe reintentarse)
     */
    private boolean isPermanentError(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            // Errores 4xx son generalmente permanentes
            return message.contains("400") || message.contains("401") || 
                   message.contains("403") || message.contains("404") ||
                   message.contains("422") || message.contains("invalid") ||
                   message.contains("unauthorized");
        }
        return false;
    }
}