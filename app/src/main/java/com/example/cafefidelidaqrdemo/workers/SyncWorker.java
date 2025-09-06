package com.example.cafefidelidaqrdemo.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.Data;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.BackoffPolicy;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.response.VisitaResponse;
import com.example.cafefidelidaqrdemo.utils.NonceManager;
import com.example.cafefidelidaqrdemo.utils.QRValidator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

/**
 * Worker para sincronizar visitas pendientes con el servidor
 * Implementa backoff exponencial y manejo de nonces expirados
 */
public class SyncWorker extends Worker {
    
    private static final String TAG = "SyncWorker";
    private static final String KEY_SYNC_TYPE = "sync_type";
    private static final String KEY_VISITA_ID = "visita_id";
    private static final String PREFS_NAME = "sync_worker_prefs";
    private static final String KEY_RETRY_COUNT = "retry_count_";
    private static final String KEY_LAST_RETRY = "last_retry_";
    
    public static final String SYNC_TYPE_ALL = "sync_all";
    public static final String SYNC_TYPE_SINGLE = "sync_single";
    
    // Configuración de backoff exponencial
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long BASE_BACKOFF_MS = TimeUnit.MINUTES.toMillis(1); // 1 minuto base
    private static final long MAX_BACKOFF_MS = TimeUnit.HOURS.toMillis(6); // Máximo 6 horas
    private static final double BACKOFF_MULTIPLIER = 2.0;
    
    // Configuración de nonce
    private static final long NONCE_EXPIRY_BUFFER_MS = TimeUnit.MINUTES.toMillis(5); // 5 minutos de buffer
    
    private final VisitaDao visitaDao;
    private final ApiService apiService;
    private final NonceManager nonceManager;
    private final SharedPreferences prefs;
    
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.visitaDao = database.visitaDao();
        this.apiService = ApiService.getInstance();
        this.nonceManager = new NonceManager(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando sincronización de visitas");
        
        try {
            String syncType = getInputData().getString(KEY_SYNC_TYPE);
            
            if (SYNC_TYPE_SINGLE.equals(syncType)) {
                long visitaId = getInputData().getLong(KEY_VISITA_ID, -1);
                if (visitaId != -1) {
                    return syncSingleVisitaWithBackoff(visitaId);
                } else {
                    Log.e(TAG, "ID de visita no válido para sincronización individual");
                    return Result.failure();
                }
            } else {
                return syncAllPendingVisitasWithBackoff();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error durante la sincronización", e);
            return Result.retry();
        }
    }
    
    /**
     * Sincroniza todas las visitas pendientes con backoff exponencial
     */
    private Result syncAllPendingVisitasWithBackoff() {
        try {
            // TODO: Corregir método para obtener visitas pendientes
            // List<VisitaEntity> visitasPendientes = visitaDao.getUltimasVisitas("PENDIENTE");
            List<VisitaEntity> visitasPendientes = visitaDao.getPendientesSync();
            
            if (visitasPendientes.isEmpty()) {
                Log.d(TAG, "No hay visitas pendientes para sincronizar");
                resetRetryCounters();
                return Result.success();
            }
            
            Log.d(TAG, "Sincronizando " + visitasPendientes.size() + " visitas pendientes");
            
            int exitosas = 0;
            int fallidas = 0;
            int noncesExpirados = 0;
            int saltadasPorBackoff = 0;
            boolean hayReintentos = false;
            
            for (VisitaEntity visita : visitasPendientes) {
                // Verificar si el nonce ha expirado
                if (isNonceExpired(visita)) {
                    handleExpiredNonce(visita);
                    noncesExpirados++;
                    continue;
                }
                
                // Verificar backoff para esta visita específica
                // TODO: Convertir ID de String a long para backoff
                // if (shouldSkipDueToBackoff(Long.parseLong(visita.getId_visita()))) {
                //     Log.d(TAG, "Saltando visita " + visita.getId_visita() + " debido a backoff");
                if (false) { // Temporalmente deshabilitado
                    Log.d(TAG, "Saltando visita " + visita.getId_visita() + " debido a backoff");
                    saltadasPorBackoff++;
                    hayReintentos = true;
                    continue;
                }
                
                Result resultado = syncVisitaWithBackoff(visita);
                
                if (resultado == Result.success()) {
                    // TODO: Convertir ID para resetRetryCount
                    // resetRetryCount(Long.parseLong(visita.getId_visita()));
                    exitosas++;
                } else if (resultado == Result.failure()) {
                    // TODO: Convertir ID para resetRetryCount
                    // resetRetryCount(Long.parseLong(visita.getId_visita()));
                    fallidas++;
                } else {
                    // TODO: Convertir ID para incrementRetryCount
                    // incrementRetryCount(Long.parseLong(visita.getId_visita()));
                    hayReintentos = true;
                }
            }
            
            Log.d(TAG, String.format("Sincronización completada - Exitosas: %d, Fallidas: %d, Nonces expirados: %d, Saltadas por backoff: %d",
                    exitosas, fallidas, noncesExpirados, saltadasPorBackoff));
            
            // Si hay reintentos pendientes, programar nueva sincronización
            if (hayReintentos) {
                scheduleRetrySync();
                return Result.retry();
            }
            
            return exitosas > 0 ? Result.success() : Result.failure();
            
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando todas las visitas", e);
            return Result.retry();
        }
    }
    
    /**
     * Sincroniza una visita específica con backoff
     */
    private Result syncSingleVisitaWithBackoff(long visitaId) {
        try {
            VisitaEntity visita = visitaDao.getById(String.valueOf(visitaId));
            
            if (visita == null) {
                Log.w(TAG, "Visita no encontrada para sincronización: " + visitaId);
                return Result.failure();
            }
            
            if (!"PENDIENTE".equals(visita.getEstado_sync())) {
                Log.d(TAG, "Visita ya sincronizada: " + visitaId);
                return Result.success();
            }
            
            // Verificar si el nonce ha expirado
            if (isNonceExpired(visita)) {
                handleExpiredNonce(visita);
                return Result.success();
            }
            
            // Verificar backoff para esta visita específica
            if (shouldSkipDueToBackoff(visitaId)) {
                Log.d(TAG, "Saltando visita " + visitaId + " debido a backoff");
                return Result.retry();
            }
            
            Result resultado = syncVisitaWithBackoff(visita);
            
            if (resultado == Result.success() || resultado == Result.failure()) {
                resetRetryCount(visitaId);
            } else {
                incrementRetryCount(visitaId);
            }
            
            return resultado;
            
        } catch (Exception e) {
            Log.e(TAG, "Error sincronizando visita individual: " + visitaId, e);
            incrementRetryCount(visitaId);
            return Result.retry();
        }
    }
    
    /**
     * Sincroniza una visita individual con el servidor con manejo mejorado de errores
     */
    private Result syncVisitaWithBackoff(VisitaEntity visita) {
        try {
            Log.d(TAG, "Sincronizando visita: " + visita.getId_visita());
            // TODO: VisitaEntity no tiene método getNonce()
            // Log.d(TAG, "Sincronizando visita: " + visita.getId_visita() + " con nonce: " + visita.getNonce());
            
            // TODO: Verificar nonce cuando VisitaEntity tenga el método
            // if (nonceManager.isNonceSynced(visita.getNonce())) {
            //     Log.w(TAG, "Nonce ya sincronizado, marcando visita como duplicada: " + visita.getNonce());
            //     markVisitaAsDuplicated(visita);
            //     return Result.success();
            // }
            
            // Crear request para la API
            ApiService.VisitaRequest request = new ApiService.VisitaRequest(
                visita.getHash_qr(),
                visita.getId_cliente(),
                visita.getFecha_hora(),
                visita.getOrigen()
            );
            
            // Llamar a la API
            Response<VisitaResponse> response = apiService.registrarVisita(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                VisitaResponse visitaResponse = response.body();
                
                if (visitaResponse.isSuccess()) {
                    // Sincronización exitosa
                    Log.d(TAG, "Visita sincronizada exitosamente: " + visita.getId_visita());
                    
                    // Actualizar visita en base de datos
                    updateVisitaAfterSync(visita, visitaResponse);
                    
                    // Marcar nonce como sincronizado
                    // TODO: Migrar nonce cuando VisitaEntity tenga el método
                // nonceManager.migrateNonceToSynced(visita.getNonce());
                    
                    return Result.success();
                    
                } else {
                    // Error de negocio (nonce duplicado, expirado, etc.)
                    Log.w(TAG, "Error de negocio al sincronizar visita: " + visitaResponse.getMessage());
                    
                    String mensaje = visitaResponse.getMessage().toLowerCase();
                    if (mensaje.contains("nonce") && mensaje.contains("expirado")) {
                        // Nonce expirado
                        handleExpiredNonce(visita);
                        return Result.success();
                    } else if (mensaje.contains("nonce") || mensaje.contains("duplicado")) {
                        // Nonce duplicado o ya usado
                        markVisitaAsDuplicated(visita);
                        return Result.success();
                    } else {
                        // Otro error de negocio
                        markVisitaAsError(visita, visitaResponse.getMessage());
                        return Result.failure();
                    }
                }
                
            } else {
                // Error de red o servidor
                Log.w(TAG, "Error de red al sincronizar visita: " + response.code());
                
                if (response.code() == 410 || response.code() == 422) {
                    // Códigos específicos para nonce expirado
                    handleExpiredNonce(visita);
                    return Result.success();
                } else if (response.code() == 409) {
                    // Conflicto - posible duplicado
                    markVisitaAsDuplicated(visita);
                    return Result.success();
                } else if (response.code() >= 400 && response.code() < 500) {
                    // Error del cliente (4xx) - no reintentar
                    String errorMsg = "Error del cliente: " + response.code();
                    markVisitaAsError(visita, errorMsg);
                    return Result.failure();
                } else {
                    // Error del servidor (5xx) o red - reintentar con backoff
                    return Result.retry();
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Excepción al sincronizar visita: " + visita.getId_visita(), e);
            // Para excepciones de red, aplicar backoff
            return Result.retry();
        }
    }
    
    /**
     * Verifica si un nonce ha expirado
     */
    private boolean isNonceExpired(VisitaEntity visita) {
        long currentTime = System.currentTimeMillis();
        long visitTime = visita.getFecha_hora();
        
        // Considerar expirado si han pasado más de 2 horas + buffer
        long expiryTime = visitTime + TimeUnit.HOURS.toMillis(2) + NONCE_EXPIRY_BUFFER_MS;
        
        return currentTime > expiryTime;
    }
    
    /**
     * Verifica si debe saltarse una visita debido a backoff
     */
    private boolean shouldSkipDueToBackoff(long visitaId) {
        int retryCount = getRetryCount(visitaId);
        
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            return true; // Máximo de intentos alcanzado
        }
        
        if (retryCount == 0) {
            return false; // Primer intento
        }
        
        long lastRetryTime = getLastRetryTime(visitaId);
        long currentTime = System.currentTimeMillis();
        
        // Calcular tiempo de backoff exponencial
        long backoffTime = calculateBackoffTime(retryCount);
        
        return (currentTime - lastRetryTime) < backoffTime;
    }
    
    /**
     * Calcula el tiempo de backoff exponencial
     */
    private long calculateBackoffTime(int retryCount) {
        double backoffMs = BASE_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, retryCount - 1);
        return Math.min((long) backoffMs, MAX_BACKOFF_MS);
    }
    
    /**
     * Maneja nonce expirado
     */
    private void handleExpiredNonce(VisitaEntity visita) {
        try {
            visita.setEstado_sync("ERROR");
            // TODO: VisitaEntity no tiene método setMensajeError
            // visita.setMensajeError("QR expirado - no se pudo sincronizar");
            visita.setFecha_hora(System.currentTimeMillis());
            
            visitaDao.update(visita);
            
            Log.w(TAG, "Nonce expirado para visita: " + visita.getId_visita());
            
        } catch (Exception e) {
            Log.e(TAG, "Error manejando nonce expirado", e);
        }
    }
    
    /**
     * Actualiza la visita después de una sincronización exitosa
     */
    private void updateVisitaAfterSync(VisitaEntity visita, VisitaResponse response) {
        try {
            visita.setEstado_sync("SINCRONIZADA");
            // TODO: VisitaEntity no tiene método setVisitaIdServidor
            // visita.setVisitaIdServidor(response.getVisitaId());
            visita.setFecha_hora(System.currentTimeMillis());
            
            // TODO: VisitaEntity no tiene métodos set() y setNeedsSync()
            // if (response.getProgreso() != null) {
            //     visita.set(response.getProgreso().getVisitasActuales());
            //     visita.setNeedsSync(response.getProgreso().getVisitasRequeridas());
            // }
            
            visitaDao.update(visita);
            
            Log.d(TAG, "Visita actualizada después de sincronización: " + visita.getId_visita());
            
        } catch (Exception e) {
            Log.e(TAG, "Error actualizando visita después de sincronización", e);
        }
    }
    
    /**
     * Marca una visita como duplicada
     */
    private void markVisitaAsDuplicated(VisitaEntity visita) {
        try {
            visita.setEstado_sync("ERROR");
        // TODO: setLastSync probablemente espera long, no String
        // visita.setLastSync("Nonce duplicado - visita ya registrada");
            visita.setFecha_hora(System.currentTimeMillis());
            
            visitaDao.update(visita);
            
            Log.d(TAG, "Visita marcada como duplicada: " + visita.getId_visita());
            
        } catch (Exception e) {
            Log.e(TAG, "Error marcando visita como duplicada", e);
        }
    }
    
    /**
     * Marca una visita con error
     */
    private void markVisitaAsError(VisitaEntity visita, String errorMessage) {
        try {
            visita.setEstado_sync("ERROR");
            // visita.setErrorMessage(errorMessage); // TODO: Agregar método setErrorMessage a VisitaEntity
            visita.setFecha_hora(System.currentTimeMillis());
            
            visitaDao.update(visita);
            
            Log.d(TAG, "Visita marcada con error: " + visita.getId_visita() + " - " + errorMessage);
            
        } catch (Exception e) {
            Log.e(TAG, "Error marcando visita con error", e);
        }
    }
    
    /**
     * Programa un reintento de sincronización
     */
    private void scheduleRetrySync() {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        Data inputData = new Data.Builder()
            .putString(KEY_SYNC_TYPE, SYNC_TYPE_ALL)
            .build();
        
        OneTimeWorkRequest retryWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build();
        
        WorkManager.getInstance(getApplicationContext()).enqueue(retryWork);
        
        Log.d(TAG, "Programado reintento de sincronización");
    }
    
    // Métodos de gestión de contadores de reintento
    
    private int getRetryCount(long visitaId) {
        return prefs.getInt(KEY_RETRY_COUNT + visitaId, 0);
    }
    
    private void incrementRetryCount(long visitaId) {
        int currentCount = getRetryCount(visitaId);
        prefs.edit()
            .putInt(KEY_RETRY_COUNT + visitaId, currentCount + 1)
            .putLong(KEY_LAST_RETRY + visitaId, System.currentTimeMillis())
            .apply();
    }
    
    private void resetRetryCount(long visitaId) {
        prefs.edit()
            .remove(KEY_RETRY_COUNT + visitaId)
            .remove(KEY_LAST_RETRY + visitaId)
            .apply();
    }
    
    private long getLastRetryTime(long visitaId) {
        return prefs.getLong(KEY_LAST_RETRY + visitaId, 0);
    }
    
    private void resetRetryCounters() {
        // Limpiar contadores antiguos (más de 7 días)
        SharedPreferences.Editor editor = prefs.edit();
        long weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_LAST_RETRY)) {
                long lastRetry = prefs.getLong(key, 0);
                if (lastRetry < weekAgo) {
                    String visitaId = key.substring(KEY_LAST_RETRY.length());
                    editor.remove(KEY_RETRY_COUNT + visitaId);
                    editor.remove(key);
                }
            }
        }
        
        editor.apply();
    }
    
    /**
     * Programa sincronización de todas las visitas pendientes
     */
    public static void scheduleSyncAll(Context context) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        Data inputData = new Data.Builder()
            .putString(KEY_SYNC_TYPE, SYNC_TYPE_ALL)
            .build();
        
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build();
        
        WorkManager.getInstance(context).enqueue(syncWork);
        
        Log.d(TAG, "Programada sincronización de todas las visitas");
    }
    
    /**
     * Programa sincronización de una visita específica
     */
    public static void scheduleSyncSingle(Context context, long visitaId) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        Data inputData = new Data.Builder()
            .putString(KEY_SYNC_TYPE, SYNC_TYPE_SINGLE)
            .putLong(KEY_VISITA_ID, visitaId)
            .build();
        
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build();
        
        WorkManager.getInstance(context).enqueue(syncWork);
        
        Log.d(TAG, "Programada sincronización de visita: " + visitaId);
    }
    
    /**
     * Fuerza la sincronización de una visita específica (saltando backoff)
     */
    public static void forceSyncVisita(Context context, long visitaId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .remove(KEY_RETRY_COUNT + visitaId)
            .remove(KEY_LAST_RETRY + visitaId)
            .apply();
        
        Log.d(TAG, "Forzando sincronización para visita: " + visitaId);
        
        // Programar sincronización inmediata
        scheduleSyncSingle(context, visitaId);
    }
}