package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.ClienteDao;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;

/**
 * Repository para manejar operaciones de Cliente con cache local y sincronización
 */
public class ClienteRepository {
    
    private final ClienteDao clienteDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para observar cambios
    private final MutableLiveData<ClienteEntity> currentClienteLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    
    public ClienteRepository(Context context) {
        this.context = context;
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.clienteDao = database.clienteDao();
        this.apiService = ApiService.getInstance();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Obtiene el cliente actual desde cache y refresca desde API
     */
    public LiveData<ClienteEntity> getCurrentCliente(String clienteId) {
        executor.execute(() -> {
            try {
                // Cargar desde cache primero
                ClienteEntity cachedCliente = clienteDao.getById(clienteId);
                if (cachedCliente != null) {
                    currentClienteLiveData.postValue(cachedCliente);
                }
                
                // Intentar refrescar desde API si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    refreshFromApi(clienteId);
                } else {
                    syncStatusLiveData.postValue(false);
                }
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al cargar cliente: " + e.getMessage());
            }
        });
        
        return currentClienteLiveData;
    }
    
    /**
     * Actualiza los datos del cliente
     */
    public void updateCliente(ClienteEntity cliente) {
        executor.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                
                if (NetworkUtils.isNetworkAvailable(context)) {
                    // Intentar enviar a API primero
                    try {
                        Cliente clienteModel = convertToModel(cliente);
                        Cliente updatedCliente = apiService.updateCliente(clienteModel);
                        
                        // Si la API responde exitosamente, actualizar cache
                        ClienteEntity updatedEntity = convertToEntity(updatedCliente);
                        updatedEntity.setSynced(true);
                        updatedEntity.setNeedsSync(false);
                        updatedEntity.setLastSync(System.currentTimeMillis());
                        
                        clienteDao.update(updatedEntity);
                        currentClienteLiveData.postValue(updatedEntity);
                        syncStatusLiveData.postValue(true);
                        
                    } catch (Exception apiException) {
                        // Si falla la API, guardar como pendiente de sincronización
                        saveAsPending(cliente);
                    }
                } else {
                    // Sin conexión, guardar como pendiente
                    saveAsPending(cliente);
                }
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al actualizar cliente: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Guarda el cliente como pendiente de sincronización
     */
    private void saveAsPending(ClienteEntity cliente) {
        cliente.setNeedsSync(true);
        cliente.setSynced(false);
        clienteDao.update(cliente);
        currentClienteLiveData.postValue(cliente);
        syncStatusLiveData.postValue(false);
        
        // Programar sincronización automática
        SyncManager.scheduleClienteSync(context);
    }
    
    /**
     * Refresca los datos desde la API
     */
    private void refreshFromApi(String clienteId) {
        try {
            Cliente clienteFromApi = apiService.getCliente(clienteId);
            ClienteEntity refreshedEntity = convertToEntity(clienteFromApi);
            refreshedEntity.setSynced(true);
            refreshedEntity.setNeedsSync(false);
            refreshedEntity.setLastSync(System.currentTimeMillis());
            
            clienteDao.update(refreshedEntity);
            currentClienteLiveData.postValue(refreshedEntity);
            syncStatusLiveData.postValue(true);
            
        } catch (Exception e) {
            // Si falla el refresh, mantener datos locales
            syncStatusLiveData.postValue(false);
        }
    }
    
    /**
     * Sincroniza todos los clientes pendientes
     */
    public void syncPendingClientes() {
        executor.execute(() -> {
            try {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    return;
                }
                
                // Obtener clientes pendientes de sincronización
                var pendingClientes = clienteDao.getPendientesSync();
                
                for (ClienteEntity cliente : pendingClientes) {
                    try {
                        Cliente clienteModel = convertToModel(cliente);
                        Cliente syncedCliente = apiService.updateCliente(clienteModel);
                        
                        // Marcar como sincronizado
                        cliente.setSynced(true);
                        cliente.setNeedsSync(false);
                        cliente.setLastSync(System.currentTimeMillis());
                        
                        clienteDao.update(cliente);
                        
                    } catch (Exception e) {
                        // Si falla un cliente específico, continuar con los demás
                        continue;
                    }
                }
                
                syncStatusLiveData.postValue(true);
                
            } catch (Exception e) {
                errorLiveData.postValue("Error en sincronización: " + e.getMessage());
            }
        });
    }
    
    /**
     * Verifica si hay conflictos de versión
     */
    public void checkForConflicts(String clienteId) {
        executor.execute(() -> {
            try {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    return;
                }
                
                ClienteEntity localCliente = clienteDao.getById(clienteId);
                Cliente remoteCliente = apiService.getCliente(clienteId);
                
                if (localCliente != null && remoteCliente != null) {
                    // Comparar timestamps para detectar conflictos
                    long localTimestamp = localCliente.getLastSync();
                    long remoteTimestamp = remoteCliente.getFechaActualizacion();
                    
                    if (remoteTimestamp > localTimestamp && localCliente.isNeedsSync()) {
                        // Hay conflicto - datos modificados en ambos lados
                        errorLiveData.postValue("CONFLICT:Los datos fueron modificados en otro dispositivo. Por favor, recarga y vuelve a intentar.");
                    }
                }
                
            } catch (Exception e) {
                // Ignorar errores de verificación de conflictos
            }
        });
    }
    
    /**
     * Convierte ClienteEntity a Cliente (modelo)
     */
    private Cliente convertToModel(ClienteEntity entity) {
        Cliente cliente = new Cliente();
        cliente.setId(entity.getId_cliente());
        cliente.setNombre(entity.getNombre());
        cliente.setEmail(entity.getEmail());
        cliente.setTelefono(entity.getTelefono());
        cliente.setFechaNacimiento(entity.getFecha_nac());
        cliente.setEstado(entity.getEstado());
        cliente.setFechaCreacion(entity.getCreado_en());
        return cliente;
    }
    
    /**
     * Convierte Cliente (modelo) a ClienteEntity
     */
    private ClienteEntity convertToEntity(Cliente cliente) {
        ClienteEntity entity = new ClienteEntity();
        entity.setId_cliente(cliente.getId());
        entity.setNombre(cliente.getNombre());
        entity.setEmail(cliente.getEmail());
        entity.setTelefono(cliente.getTelefono());
        entity.setFecha_nac(cliente.getFechaNacimiento());
        entity.setEstado(cliente.getEstado());
        entity.setCreado_en(cliente.getFechaCreacion());
        return entity;
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
     * Limpia el error actual
     */
    public void clearError() {
        errorLiveData.postValue(null);
    }
}