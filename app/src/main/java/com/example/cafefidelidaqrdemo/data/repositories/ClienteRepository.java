package com.example.cafefidelidaqrdemo.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.ClienteDao;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import com.example.cafefidelidaqrdemo.sync.SyncManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;
import retrofit2.Response;

/**
 * Repository para manejar operaciones de Cliente con cache local y sincronización
 */
public class ClienteRepository {
    
    private static ClienteRepository instance;
    private final ClienteDao clienteDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para observar cambios
    private final MutableLiveData<ClienteEntity> currentClienteLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    
    private ClienteRepository(Context context) {
        this.context = context;
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.clienteDao = database.clienteDao();
        this.apiService = ApiService.getInstance();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized ClienteRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ClienteRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    public static synchronized ClienteRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ClienteRepository must be initialized with context first");
        }
        return instance;
    }
    
    /**
     * Callback para operaciones de cliente
     */
    public interface ClienteCallback {
        void onSuccess(ClienteEntity cliente);
        void onError(String error);
    }
    
    /**
     * Obtiene cliente por ID
     */
    public void getClienteById(String clienteId, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        
        executor.execute(() -> {
            try {
                // Cargar desde cache primero
                ClienteEntity cachedCliente = clienteDao.getById(clienteId);
                if (cachedCliente != null) {
                    currentClienteLiveData.postValue(cachedCliente);
                    callback.onSuccess(cachedCliente);
                }
                
                // Refrescar desde API si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    refreshFromApi(clienteId, callback);
                } else {
                    isLoadingLiveData.postValue(false);
                    if (cachedCliente == null) {
                        callback.onError("No hay conexión y no se encontraron datos en cache");
                    }
                }
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                String error = "Error al obtener cliente: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Crea un nuevo cliente
     */
    public void createCliente(ClienteEntity cliente, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        
        executor.execute(() -> {
            try {
                // Guardar en cache local
                clienteDao.insert(cliente);
                
                // Sincronizar con API si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    // Aquí iría la llamada a la API
                    // Por ahora solo guardamos localmente
                }
                
                isLoadingLiveData.postValue(false);
                currentClienteLiveData.postValue(cliente);
                callback.onSuccess(cliente);
                
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                String error = "Error al crear cliente: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Actualiza un cliente existente
     */
    public void updateCliente(ClienteEntity cliente, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        
        executor.execute(() -> {
            try {
                // Actualizar en cache local
                clienteDao.update(cliente);
                
                // Sincronizar con API si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    // Aquí iría la llamada a la API
                    // Por ahora solo actualizamos localmente
                }
                
                isLoadingLiveData.postValue(false);
                currentClienteLiveData.postValue(cliente);
                callback.onSuccess(cliente);
                
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                String error = "Error al actualizar cliente: " + e.getMessage();
                errorLiveData.postValue(error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Refresca datos desde la API
     */
    private void refreshFromApi(String clienteId, ClienteCallback callback) {
        // Implementación de refresh desde API
        // Por ahora es un placeholder
        isLoadingLiveData.postValue(false);
    }
    
    /**
     * Obtiene el cliente actual desde cache y refresca desde API
     */
    public LiveData<ClienteEntity> getCurrentCliente(String clienteId) {
        getClienteById(clienteId, new ClienteCallback() {
            @Override
            public void onSuccess(ClienteEntity cliente) {
                // Ya se actualiza el LiveData en getClienteById
            }
            
            @Override
            public void onError(String error) {
                // Error ya se maneja en getClienteById
            }
        });
        
        return currentClienteLiveData;
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
    
    /**
     * Obtiene el estado offline del repositorio
     * @return LiveData con el estado offline
     */
    public LiveData<Boolean> getIsOffline() {
        MutableLiveData<Boolean> offlineLiveData = new MutableLiveData<>();
        Boolean syncStatus = syncStatusLiveData.getValue();
        offlineLiveData.setValue(syncStatus == null || !syncStatus);
        return offlineLiveData;
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        errorLiveData.postValue(null);
    }
    
    /**
     * Obtiene cliente por ID de forma síncrona
     */
    public ClienteEntity getClienteById(String clienteId) {
        try {
            return clienteDao.getById(clienteId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtiene cliente por email de forma síncrona
     */
    public ClienteEntity getClienteByEmailSync(String email) {
        try {
            return clienteDao.getByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }
}