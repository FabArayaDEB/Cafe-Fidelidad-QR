package com.example.cafefidelidaqrdemo.offline;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.UsuarioDao;
import com.example.cafefidelidaqrdemo.database.dao.TransaccionDao;
import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.utils.PerformanceMonitor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestor principal para arquitectura offline-first
 * Maneja cache local, sincronización automática y operación sin internet
 */
public class OfflineManager {
    private static final String TAG = "OfflineManager";
    private static final long CACHE_VALIDITY_TIME = 5 * 60 * 1000; // 5 minutos
    private static final long AUTO_SYNC_INTERVAL = 30 * 1000; // 30 segundos
    
    // Optimizaciones de rendimiento
    private static final long PERFORMANCE_TARGET_CACHE = 200; // 200ms para cache local
    private static final long PERFORMANCE_TARGET_ONLINE = 1500; // 1.5s para operaciones online
    
    private static volatile OfflineManager INSTANCE;
    private Context context;
    private CafeFidelidadDatabase database;
    private UsuarioDao usuarioDao;
    private TransaccionDao transaccionDao;
    private ExecutorService executorService;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseRef;
    
    // Cache en memoria para usuarios frecuentemente accedidos
    private final Map<String, UsuarioEntity> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryCacheTimestamps = new ConcurrentHashMap<>();
    
    private OfflineManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = CafeFidelidadDatabase.getInstance(context);
        this.usuarioDao = database.usuarioDao();
        this.transaccionDao = database.transaccionDao();
        this.executorService = Executors.newFixedThreadPool(3);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseRef = FirebaseDatabase.getInstance().getReference();
    }
    
    public static OfflineManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (OfflineManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OfflineManager(context);
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Verifica conectividad de red
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /**
     * Obtiene usuario con cache rápido (< 200ms según requisitos)
     */
    public void obtenerUsuario(String uid, UsuarioCallback callback) {
        getUsuario(uid, callback);
    }
    
    /**
     * Obtiene usuario con cache rápido optimizado (< 200ms según requisitos)
     */
    public void getUsuario(String uid, UsuarioCallback callback) {
        long startTime = PerformanceMonitor.startMeasurement("getUsuario_total");
        
        // Verificar cache en memoria primero (más rápido)
        long memoryCacheStart = PerformanceMonitor.startMeasurement("cache_memoria");
        UsuarioEntity memoryUser = getFromMemoryCache(uid);
        if (memoryUser != null) {
            PerformanceMonitor.endMeasurement("cache_memoria", memoryCacheStart);
            PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
            callback.onExito(memoryUser);
            return;
        }
        PerformanceMonitor.endMeasurement("cache_memoria", memoryCacheStart);
        
        executorService.execute(() -> {
            long dbCacheStart = PerformanceMonitor.startMeasurement("cache_database");
            
            // Intentar obtener desde cache de base de datos
            long cacheValidTime = System.currentTimeMillis() - CACHE_VALIDITY_TIME;
            UsuarioEntity cachedUser = usuarioDao.getUsuarioFromCache(uid, cacheValidTime);
            
            if (cachedUser != null) {
                PerformanceMonitor.endMeasurement("cache_database", dbCacheStart);
                
                // Guardar en cache de memoria para próximas consultas
                putInMemoryCache(uid, cachedUser);
                PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
                callback.onExito(cachedUser);
                return;
            }
            PerformanceMonitor.endMeasurement("cache_database", dbCacheStart);
            
            // Si no hay cache válido y hay internet, obtener de Firebase
            if (isNetworkAvailable()) {
                fetchUsuarioFromFirebase(uid, callback, startTime);
            } else {
                // Sin internet, usar último cache disponible
                UsuarioEntity lastCached = usuarioDao.getUsuarioById(uid);
                if (lastCached != null) {
                    putInMemoryCache(uid, lastCached);
                    PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
                    callback.onExito(lastCached);
                } else {
                    PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
                    callback.onError("No hay datos disponibles offline");
                }
            }
        });
    }
    
    /**
     * Registra transacción offline-first con optimizaciones de rendimiento
     */
    public void registrarTransaccion(TransaccionEntity transaccion, TransaccionCallback callback) {
        long startTime = PerformanceMonitor.startMeasurement("registrarTransaccion_total");
        
        executorService.execute(() -> {
            long dbInsertStart = PerformanceMonitor.startMeasurement("transaccion_insert_local");
            
            try {
                // Guardar inmediatamente en cache local
                transaccionDao.insertTransaccion(transaccion);
                
                PerformanceMonitor.endMeasurement("transaccion_insert_local", dbInsertStart);
                PerformanceMonitor.endMeasurement("registrarTransaccion_total", startTime);
                
                // Respuesta inmediata para mejor UX
                callback.onExito(transaccion.getId());
                
                // Si hay internet, sincronizar en background sin bloquear la respuesta
                if (isNetworkAvailable()) {
                    syncTransaccionToFirebaseAsync(transaccion);
                } else {
                    // Marcar para sincronización posterior
                    transaccion.setNeedsSync(true);
                    transaccionDao.updateTransaccion(transaccion);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error registrando transacción: " + e.getMessage());
                callback.onError("Error registrando transacción: " + e.getMessage());
            }
        });
    }
    
    /**
     * Actualiza usuario offline-first con optimizaciones de rendimiento
     */
    public void actualizarUsuario(UsuarioEntity usuario, UsuarioCallback callback) {
        long startTime = PerformanceMonitor.startMeasurement("actualizarUsuario_total");
        
        executorService.execute(() -> {
            try {
                long updateStart = PerformanceMonitor.startMeasurement("usuario_update_local");
                
                // Actualizar inmediatamente en cache de memoria
                putInMemoryCache(usuario.getUid(), usuario);
                
                // Actualizar en base de datos local
                usuarioDao.updateUsuario(usuario);
                
                PerformanceMonitor.endMeasurement("usuario_update_local", updateStart);
                
                // Si hay internet, sincronizar inmediatamente
                if (isNetworkAvailable()) {
                    syncUsuarioToFirebase(usuario);
                }
                
                PerformanceMonitor.endMeasurement("actualizarUsuario_total", startTime);
                
                // Respuesta inmediata con datos locales
                callback.onExito(usuario);
                
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando usuario: " + e.getMessage());
                callback.onError("Error actualizando usuario: " + e.getMessage());
            }
        });
    }
    
    /**
     * Obtiene usuario desde cache en memoria si está disponible y válido
     */
    private UsuarioEntity getFromMemoryCache(String uid) {
        Long timestamp = memoryCacheTimestamps.get(uid);
        if (timestamp != null) {
            long age = System.currentTimeMillis() - timestamp;
            if (age < CACHE_VALIDITY_TIME) {
                return memoryCache.get(uid);
            } else {
                // Cache expirado, limpiar
                memoryCache.remove(uid);
                memoryCacheTimestamps.remove(uid);
            }
        }
        return null;
    }
    
    /**
     * Guarda usuario en cache de memoria
     */
    private void putInMemoryCache(String uid, UsuarioEntity usuario) {
        memoryCache.put(uid, usuario);
        memoryCacheTimestamps.put(uid, System.currentTimeMillis());
        
        // Limpiar cache si tiene más de 50 entradas (evitar uso excesivo de memoria)
        if (memoryCache.size() > 50) {
            cleanOldMemoryCache();
        }
    }
    
    /**
     * Limpia entradas antiguas del cache de memoria
     */
    private void cleanOldMemoryCache() {
        long cutoffTime = System.currentTimeMillis() - CACHE_VALIDITY_TIME;
        memoryCacheTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue() < cutoffTime) {
                memoryCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Sincronización automática en background
     */
    public void startAutoSync() {
        executorService.execute(() -> {
            while (true) {
                try {
                    if (isNetworkAvailable()) {
                        syncPendingData();
                    }
                    Thread.sleep(AUTO_SYNC_INTERVAL);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Auto sync interrumpido", e);
                    break;
                }
            }
        });
    }
    
    /**
     * Sincroniza datos pendientes con Firebase
     */
    private void syncPendingData() {
        try {
            // Sincronizar usuarios pendientes
            List<UsuarioEntity> usuariosPendientes = usuarioDao.getUsuariosNeedingSync();
            for (UsuarioEntity usuario : usuariosPendientes) {
                syncUsuarioToFirebase(usuario);
            }
            
            // Sincronizar transacciones pendientes
            List<TransaccionEntity> transaccionesPendientes = transaccionDao.getTransaccionesNeedingSync();
            for (TransaccionEntity transaccion : transaccionesPendientes) {
                syncTransaccionToFirebase(transaccion, null);
            }
            
            Log.d(TAG, "Sincronización completada: " + usuariosPendientes.size() + 
                      " usuarios, " + transaccionesPendientes.size() + " transacciones");
                      
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización automática", e);
        }
    }
    
    /**
     * Obtiene usuario desde Firebase y actualiza cache con optimizaciones
     */
    private void fetchUsuarioFromFirebase(String uid, UsuarioCallback callback, long startTime) {
        long firebaseStart = PerformanceMonitor.startMeasurement("firebase_fetch_usuario");
        
        firebaseRef.child("Users").child(uid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    PerformanceMonitor.endMeasurement("firebase_fetch_usuario", firebaseStart);
                    
                    if (snapshot.exists()) {
                        UsuarioEntity usuario = convertToUsuarioEntity(snapshot, uid);
                        
                        // Guardar en cache de memoria inmediatamente
                        putInMemoryCache(uid, usuario);
                        
                        // Guardar en base de datos en background
                        executorService.execute(() -> {
                            long dbSaveStart = PerformanceMonitor.startMeasurement("usuario_save_local");
                            usuarioDao.insertUsuario(usuario);
                            PerformanceMonitor.endMeasurement("usuario_save_local", dbSaveStart);
                        });
                        
                        PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
                        callback.onExito(usuario);
                    } else {
                        PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
                        callback.onError("Usuario no encontrado");
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    PerformanceMonitor.endMeasurement("firebase_fetch_usuario", firebaseStart);
                    PerformanceMonitor.endMeasurement("getUsuario_total", startTime);
                    callback.onError("Error de Firebase: " + error.getMessage());
                }
            });
    }
    
    /**
     * Sincroniza transacción con Firebase
     */
    private void syncTransaccionToFirebase(TransaccionEntity transaccion, TransaccionCallback callback) {
        long syncStart = PerformanceMonitor.startMeasurement("firebase_sync_transaccion");
        HashMap<String, Object> transaccionMap = convertToHashMap(transaccion);
        
        firebaseRef.child("TransaccionesSeguras")
            .child(transaccion.getUserId())
            .child(transaccion.getHash())
            .setValue(transaccionMap)
            .addOnSuccessListener(aVoid -> {
                PerformanceMonitor.endMeasurement("firebase_sync_transaccion", syncStart);
                
                // Marcar como sincronizada
                executorService.execute(() -> {
                    transaccionDao.markAsSynced(transaccion.getId(), System.currentTimeMillis());
                });
                
                if (callback != null) {
                    callback.onExito(transaccion.getId());
                }
            })
            .addOnFailureListener(e -> {
                PerformanceMonitor.endMeasurement("firebase_sync_transaccion", syncStart);
                if (callback != null) {
                    callback.onError("Error al sincronizar: " + e.getMessage());
                }
                Log.e(TAG, "Error al sincronizar transacción", e);
            });
    }
    
    /**
     * Sincroniza transacción con Firebase de forma asíncrona sin callback
     */
    private void syncTransaccionToFirebaseAsync(TransaccionEntity transaccion) {
        syncTransaccionToFirebase(transaccion, new TransaccionCallback() {
            @Override
            public void onExito(String transaccionId) {
                // Sincronización en background completada
            }
            
            @Override
            public void onError(String error) {
                // Error en sincronización background, se reintentará después
                Log.d(TAG, "Sincronización background fallida, se reintentará: " + error);
            }
        });
    }
    
    /**
     * Sincroniza usuario con Firebase
     */
    private void syncUsuarioToFirebase(UsuarioEntity usuario) {
        HashMap<String, Object> usuarioMap = convertUsuarioToHashMap(usuario);
        
        firebaseRef.child("Users").child(usuario.getUid())
            .updateChildren(usuarioMap)
            .addOnSuccessListener(aVoid -> {
                usuarioDao.markAsSynced(usuario.getUid(), System.currentTimeMillis());
                Log.d(TAG, "Usuario sincronizado con Firebase");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al sincronizar usuario", e);
            });
    }
    
    // Métodos de conversión
    private UsuarioEntity convertToUsuarioEntity(DataSnapshot snapshot, String uid) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUid(uid);
        usuario.setNames(snapshot.child("names").getValue(String.class));
        usuario.setEmail(snapshot.child("email").getValue(String.class));
        usuario.setProveedor(snapshot.child("proveedor").getValue(String.class));
        usuario.setEstado(snapshot.child("estado").getValue(String.class));
        usuario.setImagen(snapshot.child("imagen").getValue(String.class));
        usuario.setDate(snapshot.child("date").getValue(Long.class));
        usuario.setPuntos(snapshot.child("puntos").getValue(Integer.class));
        usuario.setNivel(snapshot.child("nivel").getValue(String.class));
        usuario.setTotalCompras(snapshot.child("totalCompras").getValue(Double.class));
        usuario.setUltimaVisita(snapshot.child("ultimaVisita").getValue(Long.class));
        usuario.setTelefono(snapshot.child("telefono").getValue(String.class));
        usuario.setFechaNacimiento(snapshot.child("fechaNacimiento").getValue(String.class));
        usuario.setLastSync(System.currentTimeMillis());
        usuario.setNeedsSync(false);
        return usuario;
    }
    
    private HashMap<String, Object> convertToHashMap(TransaccionEntity transaccion) {
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
    
    /**
     * Inicia sincronización inmediata de datos pendientes
     */
    public void iniciarSincronizacion() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Sin conexión a internet, sincronización pospuesta");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // Sincronizar usuarios pendientes
                List<UsuarioEntity> usuariosPendientes = usuarioDao.getUsuariosNeedingSync();
                for (UsuarioEntity usuario : usuariosPendientes) {
                    syncUsuarioToFirebase(usuario);
                }
                
                // Sincronizar transacciones pendientes
                List<TransaccionEntity> transaccionesPendientes = transaccionDao.getTransaccionesNeedingSync();
                for (TransaccionEntity transaccion : transaccionesPendientes) {
                    syncTransaccionToFirebase(transaccion, null);
                }
                
                Log.d(TAG, "Sincronización completada: " + usuariosPendientes.size() + 
                          " usuarios, " + transaccionesPendientes.size() + " transacciones");
                          
            } catch (Exception e) {
                Log.e(TAG, "Error en sincronización: " + e.getMessage());
            }
        });
    }
    
    // Interfaces de callback
    public interface UsuarioCallback {
        void onExito(UsuarioEntity usuario);
        void onError(String error);
    }
    
    public interface TransaccionCallback {
        void onExito(String transaccionId);
        void onError(String error);
    }
}