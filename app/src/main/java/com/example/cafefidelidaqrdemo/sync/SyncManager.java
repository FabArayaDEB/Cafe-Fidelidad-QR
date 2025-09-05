package com.example.cafefidelidaqrdemo.sync;

import android.content.Context;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

/**
 * Gestor de sincronización automática usando WorkManager
 */
public class SyncManager {
    
    private static final String CLIENTE_SYNC_WORK = "cliente_sync_work";
    private static final String VISITA_SYNC_WORK = "visita_sync_work";
    private static final String CANJE_SYNC_WORK = "canje_sync_work";
    private static final String PERIODIC_SYNC_WORK = "periodic_sync_work";
    
    /**
     * Programa sincronización inmediata de cliente
     */
    public static void scheduleClienteSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(ClienteSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(CLIENTE_SYNC_WORK)
                .build();
        
        WorkManager.getInstance(context)
                .enqueueUniqueWork(CLIENTE_SYNC_WORK, ExistingWorkPolicy.REPLACE, syncWork);
    }
    
    /**
     * Programa sincronización inmediata de visitas
     */
    public static void scheduleVisitaSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(VisitaSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(VISITA_SYNC_WORK)
                .build();
        
        WorkManager.getInstance(context)
                .enqueueUniqueWork(VISITA_SYNC_WORK, ExistingWorkPolicy.REPLACE, syncWork);
    }
    
    /**
     * Programa sincronización inmediata de canjes
     */
    public static void scheduleCanjeSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(CanjeSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(CANJE_SYNC_WORK)
                .build();
        
        WorkManager.getInstance(context)
                .enqueueUniqueWork(CANJE_SYNC_WORK, ExistingWorkPolicy.REPLACE, syncWork);
    }
    
    /**
     * Programa sincronización periódica (cada 2 horas)
     */
    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        
        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(
                PeriodicSyncWorker.class, 2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .addTag(PERIODIC_SYNC_WORK)
                .build();
        
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC_SYNC_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicWork);
    }
    
    /**
     * Cancela todas las sincronizaciones programadas
     */
    public static void cancelAllSync(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelUniqueWork(CLIENTE_SYNC_WORK);
        workManager.cancelUniqueWork(VISITA_SYNC_WORK);
        workManager.cancelUniqueWork(CANJE_SYNC_WORK);
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK);
    }
    
    /**
     * Fuerza sincronización inmediata de todos los datos pendientes
     */
    public static void forceSyncAll(Context context) {
        scheduleClienteSync(context);
        scheduleVisitaSync(context);
        scheduleCanjeSync(context);
    }
    
    /**
     * Verifica el estado de sincronización
     */
    public static void checkSyncStatus(Context context, SyncStatusCallback callback) {
        WorkManager workManager = WorkManager.getInstance(context);
        
        workManager.getWorkInfosForUniqueWorkLiveData(CLIENTE_SYNC_WORK)
                .observeForever(workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        callback.onSyncStatus("cliente", workInfo.getState());
                    }
                });
        
        workManager.getWorkInfosForUniqueWorkLiveData(VISITA_SYNC_WORK)
                .observeForever(workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        callback.onSyncStatus("visita", workInfo.getState());
                    }
                });
        
        workManager.getWorkInfosForUniqueWorkLiveData(CANJE_SYNC_WORK)
                .observeForever(workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        callback.onSyncStatus("canje", workInfo.getState());
                    }
                });
    }
    
    /**
     * Interface para callbacks de estado de sincronización
     */
    public interface SyncStatusCallback {
        void onSyncStatus(String syncType, WorkInfo.State state);
    }
}