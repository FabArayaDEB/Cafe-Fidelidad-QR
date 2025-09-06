package com.example.cafefidelidaqrdemo.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.CanjeDao;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.example.cafefidelidaqrdemo.models.Canje;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Response;
import java.util.List;

/**
 * Worker para sincronización de canjes en segundo plano
 */
public class CanjeSyncWorker extends Worker {
    
    private final CanjeDao canjeDao;
    private final ApiService apiService;
    
    public CanjeSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.canjeDao = database.canjeDao();
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
            
            // Obtener canjes pendientes de sincronización
            List<CanjeEntity> pendingCanjes = canjeDao.getCanjesParaSincronizar();
            
            if (pendingCanjes.isEmpty()) {
                return Result.success();
            }
            
            int successCount = 0;
            int totalCount = pendingCanjes.size();
            
            for (CanjeEntity canje : pendingCanjes) {
                try {
                    // Convertir a modelo para API
                    Canje canjeModel = convertToModel(canje);
                    
                    // Enviar a API
                    Call<Canje> call = apiService.createCanje(canjeModel);
                    Response<Canje> response = call.execute();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Canje syncedCanje = response.body();
                        
                        // Actualizar con ID del servidor si es necesario
                        if (syncedCanje.getId() != null && !syncedCanje.getId().equals(canje.getId_canje())) {
                            canje.setId_canje(syncedCanje.getId());
                        }
                        
                        // Actualizar estado de sincronización
                        canje.setSynced(true);
                        canje.setNeedsSync(false);
                        canje.setLastSync(System.currentTimeMillis());
                    } else {
                        throw new Exception("Error en respuesta de API: " + response.code());
                    }
                    
                    // Actualizar en base de datos local
                    canjeDao.update(canje);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    // Log del error pero continuar con otros canjes
                    android.util.Log.e("CanjeSyncWorker", "Error sincronizando canje " + canje.getId_canje(), e);
                    
                    // Marcar como error si es un error permanente
                    if (isPermanentError(e)) {
                        canje.setSynced(false);
                        canje.setNeedsSync(true);
                        canjeDao.update(canje);
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
            android.util.Log.e("CanjeSyncWorker", "Error general en sincronización de canjes", e);
            return Result.retry();
        }
    }
    
    /**
     * Convierte CanjeEntity a Canje (modelo para API)
     */
    private Canje convertToModel(CanjeEntity entity) {
        Canje canje = new Canje();
        canje.setId(entity.getId_canje());
        canje.setUserId(entity.getId_cliente());
        canje.setFechaCanje(entity.getFecha_solicitud());
        canje.setSucursal(entity.getId_sucursal());
        canje.setCodigoVerificacion(entity.getOtp_codigo());
        return canje;
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
                   message.contains("unauthorized") || message.contains("expired");
        }
        return false;
    }
}