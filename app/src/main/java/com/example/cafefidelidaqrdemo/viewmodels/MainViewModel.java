package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

import com.example.cafefidelidaqrdemo.data.repositories.AuthRepository;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;

/**
 * ViewModel para MainActivity
 * Maneja la lógica de navegación y estado de autenticación
 */
public class MainViewModel extends AndroidViewModel {
    
    private final AuthRepository authRepository;
    private final ClienteRepository clienteRepository;
    
    // StateFlow para el título de la toolbar
    private final MutableStateFlow<String> _toolbarTitle = StateFlowKt.MutableStateFlow("Mi Perfil");
    public StateFlow<String> toolbarTitle = _toolbarTitle;
    
    // StateFlow para el estado de autenticación
    private final MutableStateFlow<Boolean> _isAuthenticated = StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> isAuthenticated = _isAuthenticated;
    
    // StateFlow para el cliente actual
    private final MutableStateFlow<ClienteEntity> _currentCliente = StateFlowKt.MutableStateFlow(null);
    public StateFlow<ClienteEntity> currentCliente = _currentCliente;
    
    // StateFlow para errores
    private final MutableStateFlow<String> _error = StateFlowKt.MutableStateFlow(null);
    public StateFlow<String> error = _error;
    
    // StateFlow para estado de carga
    private final MutableStateFlow<Boolean> _isLoading = StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> isLoading = _isLoading;
    
    // MutableLiveData para compatibilidad con Data Binding
    private final MutableLiveData<String> toolbarTitleLiveData = new MutableLiveData<>();
    private final MutableLiveData<ClienteEntity> currentClienteLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticatedLiveData = new MutableLiveData<>();
    
    public MainViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance();
        this.clienteRepository = new ClienteRepository(application);
        
        // El título por defecto ya está inicializado en el StateFlow
        
        // Verificar estado de autenticación
        checkAuthenticationStatus();
    }
    
    /**
     * Verifica el estado de autenticación del usuario
     */
    public void checkAuthenticationStatus() {
        _isLoading.setValue(true);
        
        authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                if (userId != null) {
                    _isAuthenticated.setValue(true);
                    loadCurrentCliente(userId);
                } else {
                    _isAuthenticated.setValue(false);
                    _isLoading.setValue(false);
                }
            }
            
            @Override
            public void onError(String error) {
                _isAuthenticated.setValue(false);
                _error.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Carga los datos del cliente actual
     */
    private void loadCurrentCliente(String userId) {
        clienteRepository.getClienteById(userId, new com.example.cafefidelidaqrdemo.repository.ClienteRepository.ClienteCallback() {
            public void onSuccess(ClienteEntity cliente) {
                _currentCliente.setValue(cliente);
                _isLoading.setValue(false);
            }
            
            public void onError(String error) {
                _error.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Actualiza el título de la toolbar
     */
    public void setToolbarTitle(String title) {
        _toolbarTitle.setValue(title);
    }
    
    /**
     * Cierra la sesión del usuario
     */
    public void logout() {
        authRepository.logout();
        _isAuthenticated.setValue(false);
        _currentCliente.setValue(null);
    }
    
    /**
     * Limpia el mensaje de error
     */
    public void clearError() {
        _error.setValue(null);
    }
    
    // Getters para StateFlow
    public StateFlow<String> getToolbarTitle() {
        return toolbarTitle;
    }
    
    public StateFlow<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }
    
    public StateFlow<ClienteEntity> getCurrentCliente() {
        return currentCliente;
    }
    
    public StateFlow<String> getError() {
        return error;
    }
    
    public StateFlow<Boolean> getIsLoading() {
        return isLoading;
    }
    
    // Getters para Data Binding (LiveData)
    public LiveData<String> getToolbarTitleLiveData() {
        return toolbarTitleLiveData;
    }
    
    public LiveData<Boolean> getIsAuthenticatedLiveData() {
        return isAuthenticatedLiveData;
    }
    
    public LiveData<ClienteEntity> getCurrentClienteLiveData() {
        return currentClienteLiveData;
    }
    
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }
}