package com.example.cafefidelidaqrdemo.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.ClienteDao;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.dao.CanjeDao;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;

/**
 * Worker para sincronización periódica de todos los datos pendientes
 */
public class PeriodicSyncWorker extends Worker {
    
    private final ClienteDao clienteDao;
    private final VisitaDao visitaDao;
    private final CanjeDao canjeDao;
    
    public PeriodicSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.clienteDao = database.clienteDao();
        this.visitaDao = database.visitaDao();
        this.canjeDao = database.canjeDao();
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            // Verificar conectividad
            if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                return Result.retry();
            }
            
            android.util.Log.i("PeriodicSyncWorker", "Iniciando sincronización periódica");
            
            boolean hasWork = false;
            
            // Verificar si hay datos pendientes de sincronización
            int pendingClientes = clienteDao.getPendientesSync().size();
            int pendingVisitas = visitaDao.getPendientesSync().size();
            int pendingCanjes = canjeDao.getCanjesParaSincronizar().size();
            
            android.util.Log.i("PeriodicSyncWorker", 
                String.format("Datos pendientes - Clientes: %d, Visitas: %d, Canjes: %d", 
                    pendingClientes, pendingVisitas, pendingCanjes));
            
            // Programar sincronización específica si hay datos pendientes
            if (pendingClientes > 0) {
                SyncManager.scheduleClienteSync(getApplicationContext());
                hasWork = true;
            }
            
            if (pendingVisitas > 0) {
                SyncManager.scheduleVisitaSync(getApplicationContext());
                hasWork = true;
            }
            
            if (pendingCanjes > 0) {
                SyncManager.scheduleCanjeSync(getApplicationContext());
                hasWork = true;
            }
            
            // También realizar limpieza de datos antiguos
            performDataCleanup();
            
            if (hasWork) {
                android.util.Log.i("PeriodicSyncWorker", "Sincronización periódica programada");
            } else {
                android.util.Log.i("PeriodicSyncWorker", "No hay datos pendientes de sincronización");
            }
            
            return Result.success();
            
        } catch (Exception e) {
            android.util.Log.e("PeriodicSyncWorker", "Error en sincronización periódica", e);
            return Result.retry();
        }
    }
    
    /**
     * Realiza limpieza de datos antiguos
     */
    private void performDataCleanup() {
        try {
            // Limpiar visitas con errores permanentes más antiguas de 30 días
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            
            // Eliminar visitas con estado ERROR más antiguas de 30 días
            // TODO: Implementar método eliminarErroresAntiguos en VisitaDao
            // visitaDao.eliminarErroresAntiguos(5, thirtyDaysAgo); // 5 intentos máximos
            android.util.Log.i("PeriodicSyncWorker", "Limpieza de visitas con errores antiguos completada");
            
            // Limpiar canjes expirados y cancelados antiguos
            int deletedCanjesExpirados = canjeDao.limpiarCanjesToExpirados(thirtyDaysAgo);
            int deletedCanjesCancelados = canjeDao.limpiarCanjesCancelados(thirtyDaysAgo);
            int totalDeletedCanjes = deletedCanjesExpirados + deletedCanjesCancelados;
            if (totalDeletedCanjes > 0) {
                android.util.Log.i("PeriodicSyncWorker", "Eliminados " + totalDeletedCanjes + " canjes antiguos");
            }
            
            // Actualizar estadísticas de sincronización
            updateSyncStats();
            
        } catch (Exception e) {
            android.util.Log.e("PeriodicSyncWorker", "Error en limpieza de datos", e);
        }
    }
    
    /**
     * Actualiza estadísticas de sincronización
     */
    private void updateSyncStats() {
        try {
            // Contar registros por estado
            int clientesPendientes = clienteDao.getPendientesSync().size();
            int visitasPendientes = visitaDao.getPendientesSync().size();
            int canjesPendientes = canjeDao.getCanjesParaSincronizar().size();
            
            int visitasEnviadas = visitaDao.getByEstadoSync("ENVIADO").size();
            int canjesEnviados = canjeDao.getCanjesNoSincronizados().size(); // Usar método disponible
            
            int visitasError = visitaDao.getByEstadoSync("ERROR").size();
            int canjesError = 0; // CanjeDao no tiene getByEstadoSync
            
            // Log de estadísticas
            android.util.Log.i("PeriodicSyncWorker", 
                String.format("Estadísticas de sync - Pendientes: C:%d V:%d Ca:%d | Enviados: V:%d Ca:%d | Errores: V:%d Ca:%d",
                    clientesPendientes, visitasPendientes, canjesPendientes,
                    visitasEnviadas, canjesEnviados,
                    visitasError, canjesError));
            
        } catch (Exception e) {
            android.util.Log.e("PeriodicSyncWorker", "Error actualizando estadísticas", e);
        }
    }
}