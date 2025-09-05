package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;

/**
 * ViewModel para el perfil del cliente
 */
public class PerfilViewModel extends AndroidViewModel {
    
    private final ClienteRepository clienteRepository;
    
    // LiveData para el cliente actual
    private final MutableLiveData<String> clienteIdLiveData = new MutableLiveData<>();
    private final LiveData<ClienteEntity> clienteLiveData;
    
    // Estados de la UI
    private final MutableLiveData<Boolean> saveSuccessLiveData = new MutableLiveData<>();
    
    public PerfilViewModel(@NonNull Application application) {
        super(application);
        this.clienteRepository = new ClienteRepository(application);
        
        // Configurar LiveData reactivo para el cliente
        this.clienteLiveData = Transformations.switchMap(clienteIdLiveData, clienteId -> {
            if (clienteId != null) {
                return clienteRepository.getCurrentCliente(clienteId);
            }
            return new MutableLiveData<>(null);
        });
    }
    
    /**
     * Carga los datos del cliente
     */
    public void loadCliente(String clienteId) {
        clienteIdLiveData.setValue(clienteId);
    }
    
    /**
     * Actualiza los datos del cliente
     */
    public void updateCliente(ClienteEntity cliente) {
        clienteRepository.updateCliente(cliente);
        // Observar el resultado y notificar éxito
        saveSuccessLiveData.setValue(true);
    }
    
    /**
     * Verifica conflictos de versión
     */
    public void checkForConflicts(String clienteId) {
        clienteRepository.checkForConflicts(clienteId);
    }
    
    /**
     * Fuerza la sincronización
     */
    public void forceSync() {
        clienteRepository.syncPendingClientes();
    }
    
    /**
     * Limpia el error actual
     */
    public void clearError() {
        clienteRepository.clearError();
    }
    
    /**
     * Resetea el estado de éxito
     */
    public void resetSaveSuccess() {
        saveSuccessLiveData.setValue(false);
    }
    
    // Getters para LiveData
    public LiveData<ClienteEntity> getCliente() {
        return clienteLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return clienteRepository.getIsLoading();
    }
    
    public LiveData<String> getError() {
        return clienteRepository.getError();
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return clienteRepository.getSyncStatus();
    }
    
    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccessLiveData;
    }
}