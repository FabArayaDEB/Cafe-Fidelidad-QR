package com.example.cafefidelidaqrdemo.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.TransaccionDao;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.data.model.Transaccion;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.Date;

/**
 * Repository para manejar operaciones de transacciones
 */
public class TransaccionRepository {
    
    private static TransaccionRepository instance;
    private final TransaccionDao transaccionDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para observar cambios
    private final MutableLiveData<List<TransaccionEntity>> transaccionesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    
    private TransaccionRepository(Context context) {
        this.context = context;
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.transaccionDao = database.transaccionDao();
        this.apiService = ApiService.getInstance();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized TransaccionRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TransaccionRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    public static synchronized TransaccionRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TransaccionRepository must be initialized with context first");
        }
        return instance;
    }
    
    /**
     * Callback para operaciones de transacción
     */
    public interface TransaccionCallback {
        void onSuccess(TransaccionEntity transaccion);
        void onError(String error);
    }
    
    /**
     * Callback para listas de transacciones
     */
    public interface TransaccionListCallback {
        void onSuccess(List<TransaccionEntity> transacciones);
        void onError(String error);
    }
    
    /**
     * Crea una nueva transacción
     */
    public void createTransaccion(TransaccionEntity transaccion, TransaccionCallback callback) {
        isLoadingLiveData.postValue(true);
        
        executor.execute(() -> {
            try {
                // Guardar en cache local
                transaccionDao.insertTransaccion(transaccion);
                
                // Sincronizar con API si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    // Aquí iría la llamada a la API
                    // Por ahora solo guardamos localmente
                }
                
                isLoadingLiveData.postValue(false);
                callback.onSuccess(transaccion);
                
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                String error = "Error al crear transacción: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene transacciones por usuario
     */
    public void getTransaccionesByUsuario(String userId, TransaccionListCallback callback) {
        isLoadingLiveData.postValue(true);
        
        executor.execute(() -> {
            try {
                List<TransaccionEntity> transacciones = transaccionDao.getTransaccionesByUserId(userId);
                
                isLoadingLiveData.postValue(false);
                transaccionesLiveData.postValue(transacciones);
                callback.onSuccess(transacciones);
                
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                String error = "Error al obtener transacciones: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene transacción por ID
     */
    public void getTransaccionById(String transaccionId, TransaccionCallback callback) {
        executor.execute(() -> {
            try {
                TransaccionEntity transaccion = transaccionDao.getTransaccionById(transaccionId);
                
                if (transaccion != null) {
                    callback.onSuccess(transaccion);
                } else {
                    callback.onError("Transacción no encontrada");
                }
                
            } catch (Exception e) {
                String error = "Error al obtener transacción: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Actualiza una transacción
     */
    public void updateTransaccion(TransaccionEntity transaccion, TransaccionCallback callback) {
        executor.execute(() -> {
            try {
                transaccionDao.updateTransaccion(transaccion);
                callback.onSuccess(transaccion);
                
            } catch (Exception e) {
                String error = "Error al actualizar transacción: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene todas las transacciones
     */
    public LiveData<List<TransaccionEntity>> getAllTransacciones() {
        // Crear LiveData manualmente ya que el DAO no tiene getAll()
        MutableLiveData<List<TransaccionEntity>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            // Por ahora retornamos lista vacía, se puede implementar según necesidades
            liveData.postValue(new java.util.ArrayList<>());
        });
        return liveData;
    }
    
    /**
     * Obtiene transacciones por usuario como LiveData
     */
    public LiveData<List<TransaccionEntity>> getTransaccionesByUsuarioLiveData(String userId) {
        // Crear LiveData manualmente ya que el DAO no tiene este método
        MutableLiveData<List<TransaccionEntity>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<TransaccionEntity> transacciones = transaccionDao.getTransaccionesByUserId(userId);
            liveData.postValue(transacciones);
        });
        return liveData;
    }
    
    /**
     * Obtiene transacciones por cliente
     */
    public void getTransaccionesByCliente(String clienteId, TransaccionListCallback callback) {
        isLoadingLiveData.postValue(true);
        
        executor.execute(() -> {
            try {
                List<TransaccionEntity> transacciones = transaccionDao.getTransaccionesByUserId(clienteId);
                
                isLoadingLiveData.postValue(false);
                transaccionesLiveData.postValue(transacciones);
                callback.onSuccess(transacciones);
                
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                String error = "Error al obtener transacciones del cliente: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    public LiveData<List<TransaccionEntity>> getTransacciones() {
        return transaccionesLiveData;
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        errorLiveData.postValue(null);
    }
    
    /**
     * Sincroniza transacciones pendientes
     */
    public void syncPendingTransacciones() {
        executor.execute(() -> {
            try {
                List<TransaccionEntity> pendientes = transaccionDao.getTransaccionesNeedingSync();
                
                for (TransaccionEntity transaccion : pendientes) {
                    // Aquí iría la lógica de sincronización con la API
                    // Por ahora solo marcamos como sincronizadas
                    transaccion.setSynced(true);
                    transaccion.setNeedsSync(false);
                    transaccionDao.updateTransaccion(transaccion);
                }
                
            } catch (Exception e) {
                String error = "Error al sincronizar transacciones: " + e.getMessage();
                errorLiveData.postValue(error);
            }
        });
    }
}