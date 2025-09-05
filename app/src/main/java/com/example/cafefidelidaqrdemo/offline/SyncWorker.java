package com.example.cafefidelidaqrdemo.offline;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.UsuarioDao;
import com.example.cafefidelidaqrdemo.database.dao.TransaccionDao;
import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Worker para sincronización automática en background
 * Funciona incluso cuando la app está cerrada
 */
public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private static final String WORK_NAME = "sync_work";
    
    private CafeFidelidadDatabase database;
    private UsuarioDao usuarioDao;
    private TransaccionDao transaccionDao;
    private DatabaseReference firebaseRef;
    
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.database = CafeFidelidadDatabase.getInstance(context);
        this.usuarioDao = database.usuarioDao();
        this.transaccionDao = database.transaccionDao();
        this.firebaseRef = FirebaseDatabase.getInstance().getReference();
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando sincronización en background");
        
        try {
            // Sincronizar usuarios pendientes
            int usuariosSincronizados = syncUsuariosPendientes();
            
            // Sincronizar transacciones pendientes
            int transaccionesSincronizadas = syncTransaccionesPendientes();
            
            // Limpiar datos antiguos para optimizar espacio
            limpiarDatosAntiguos();
            
            Log.d(TAG, "Sincronización completada: " + usuariosSincronizados + 
                      " usuarios, " + transaccionesSincronizadas + " transacciones");
            
            return Result.success();
            
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización background", e);
            return Result.retry();
        }
    }
    
    /**
     * Sincroniza usuarios que necesitan actualización
     */
    private int syncUsuariosPendientes() {
        List<UsuarioEntity> usuariosPendientes = usuarioDao.getUsuariosNeedingSync();
        int sincronizados = 0;
        
        for (UsuarioEntity usuario : usuariosPendientes) {
            try {
                HashMap<String, Object> usuarioMap = convertUsuarioToHashMap(usuario);
                
                // Sincronización síncrona para WorkManager
                firebaseRef.child("Users").child(usuario.getUid())
                    .updateChildren(usuarioMap)
                    .addOnSuccessListener(aVoid -> {
                        usuarioDao.markAsSynced(usuario.getUid(), System.currentTimeMillis());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al sincronizar usuario: " + usuario.getUid(), e);
                    });
                
                sincronizados++;
                
            } catch (Exception e) {
                Log.e(TAG, "Error procesando usuario: " + usuario.getUid(), e);
            }
        }
        
        return sincronizados;
    }
    
    /**
     * Sincroniza transacciones pendientes
     */
    private int syncTransaccionesPendientes() {
        List<TransaccionEntity> transaccionesPendientes = transaccionDao.getTransaccionesNeedingSync();
        int sincronizadas = 0;
        
        for (TransaccionEntity transaccion : transaccionesPendientes) {
            try {
                HashMap<String, Object> transaccionMap = convertTransaccionToHashMap(transaccion);
                
                firebaseRef.child("TransaccionesSeguras")
                    .child(transaccion.getUserId())
                    .child(transaccion.getHash())
                    .setValue(transaccionMap)
                    .addOnSuccessListener(aVoid -> {
                        transaccionDao.markAsSynced(transaccion.getId(), System.currentTimeMillis());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al sincronizar transacción: " + transaccion.getId(), e);
                    });
                
                sincronizadas++;
                
            } catch (Exception e) {
                Log.e(TAG, "Error procesando transacción: " + transaccion.getId(), e);
            }
        }
        
        return sincronizadas;
    }
    
    /**
     * Limpia datos antiguos para optimizar espacio de almacenamiento
     */
    private void limpiarDatosAntiguos() {
        try {
            // Eliminar transacciones sincronizadas más antiguas de 30 días
            long fechaLimite = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            transaccionDao.limpiarTransaccionesAntiguas(fechaLimite);
            
            Log.d(TAG, "Limpieza de datos antiguos completada");
            
        } catch (Exception e) {
            Log.e(TAG, "Error en limpieza de datos antiguos", e);
        }
    }
    
    /**
     * Programa la sincronización periódica
     */
    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build();
        
        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 
                15, // Cada 15 minutos (mínimo permitido)
                TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build();
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            );
        
        Log.d(TAG, "Sincronización periódica programada");
    }
    
    /**
     * Cancela la sincronización periódica
     */
    public static void cancelPeriodicSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
        Log.d(TAG, "Sincronización periódica cancelada");
    }
    
    // Métodos de conversión
    private HashMap<String, Object> convertUsuarioToHashMap(UsuarioEntity usuario) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("names", usuario.getNames());
        map.put("email", usuario.getEmail());
        map.put("puntos", usuario.getPuntos());
        map.put("nivel", usuario.getNivel());
        map.put("totalCompras", usuario.getTotalCompras());
        map.put("ultimaVisita", usuario.getUltimaVisita());
        map.put("telefono", usuario.getTelefono());
        map.put("fechaNacimiento", usuario.getFechaNacimiento());
        return map;
    }
    
    private HashMap<String, Object> convertTransaccionToHashMap(TransaccionEntity transaccion) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("sucursalId", transaccion.getSucursalId());
        map.put("mesaId", transaccion.getMesaId());
        map.put("monto", transaccion.getMonto());
        map.put("puntos", transaccion.getPuntos());
        map.put("fecha", transaccion.getFecha());
        map.put("qrTimestamp", transaccion.getQrTimestamp());
        map.put("hash", transaccion.getHash());
        map.put("jwtUsed", transaccion.isJwtUsed());
        map.put("descripcion", transaccion.getDescripcion());
        return map;
    }
}