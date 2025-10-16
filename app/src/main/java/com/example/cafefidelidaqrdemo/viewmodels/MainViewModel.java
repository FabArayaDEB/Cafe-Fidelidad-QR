package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;

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
    
    // StateFlow para el estado de autenticación - inicia como false para forzar verificación
    private final MutableStateFlow<Boolean> _isAuthenticated = StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> isAuthenticated = _isAuthenticated;
    
    // StateFlow para el cliente actual
    private final MutableStateFlow<Cliente> _currentCliente = StateFlowKt.MutableStateFlow(null);
    public StateFlow<Cliente> currentCliente = _currentCliente;
    
    // StateFlow para errores
    private final MutableStateFlow<String> _error = StateFlowKt.MutableStateFlow(null);
    public StateFlow<String> error = _error;
    
    // StateFlow para estado de carga
    private final MutableStateFlow<Boolean> _isLoading = StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> isLoading = _isLoading;
    
    // MutableLiveData para compatibilidad con Data Binding
    private final MutableLiveData<String> toolbarTitleLiveData = new MutableLiveData<>();
    private final MutableLiveData<Cliente> currentClienteLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticatedLiveData = new MutableLiveData<>();
    
    public MainViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance();
        this.clienteRepository = new ClienteRepository(application);
        
        // Establecer contexto en AuthRepository para SessionManager
        authRepository.setContext(application);
        
        // Sincronizar StateFlow con LiveData
        setupLiveDataSync();
        
        // El título por defecto ya está inicializado en el StateFlow
        
        // Verificar estado de autenticación
        checkAuthenticationStatus();
    }
    
    /**
     * Configura la sincronización entre StateFlow y LiveData
     */
    private void setupLiveDataSync() {
        // Por simplicidad, vamos a actualizar ambos StateFlow y LiveData en los métodos
        // Inicializar LiveData: isAuthenticated en null para evitar redirección prematura
        isAuthenticatedLiveData.setValue(null);
        errorLiveData.setValue(null);
        isLoadingLiveData.setValue(false);
        toolbarTitleLiveData.setValue("Mi Perfil");
        currentClienteLiveData.setValue(null);
    }
    
    /**
     * Verifica el estado de autenticación del usuario
     */
    public void checkAuthenticationStatus() {
        _isLoading.setValue(true);
        isLoadingLiveData.setValue(true);
        
        // Verificar si hay usuario logueado (incluye sesión persistente)
        boolean isLoggedIn = authRepository.isUserLoggedIn();
        android.util.Log.d("MainViewModel", "checkAuthenticationStatus - isUserLoggedIn: " + isLoggedIn);
        
        if (isLoggedIn) {
            // Usuario autenticado, verificar si es usuario local o Firebase
            AuthRepository.LocalUser localUser = authRepository.getCurrentUser();
            if (localUser != null) {
                // Usuario local autenticado
                android.util.Log.d("MainViewModel", "Usuario local autenticado: " + localUser.name + " (" + localUser.role + ")");
                _isAuthenticated.setValue(true);
                isAuthenticatedLiveData.setValue(true);
                _isLoading.setValue(false);
                isLoadingLiveData.setValue(false);
                
                // Para usuarios locales, no necesitamos cargar ClienteEntity
                // El sistema funciona solo con la información del LocalUser
            } else {
                // Intentar obtener usuario con callback (para compatibilidad con Firebase)
                authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
                    @Override
                    public void onSuccess(String userId) {
                        if (userId != null) {
                            android.util.Log.d("MainViewModel", "Usuario autenticado con ID: " + userId);
                            _isAuthenticated.setValue(true);
                            isAuthenticatedLiveData.setValue(true);
                            
                            // Solo cargar ClienteEntity si no es usuario local
                            if (!userId.startsWith("user_") && !userId.startsWith("admin_")) {
                                loadCurrentCliente(userId);
                            } else {
                                _isLoading.setValue(false);
                                isLoadingLiveData.setValue(false);
                            }
                        } else {
                            android.util.Log.d("MainViewModel", "Usuario ID es null");
                            _isAuthenticated.setValue(false);
                            isAuthenticatedLiveData.setValue(false);
                            _isLoading.setValue(false);
                            isLoadingLiveData.setValue(false);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        android.util.Log.d("MainViewModel", "Error al obtener usuario: " + error);
                        _isAuthenticated.setValue(false);
                        isAuthenticatedLiveData.setValue(false);
                        _error.setValue(error);
                        errorLiveData.setValue(error);
                        _isLoading.setValue(false);
                        isLoadingLiveData.setValue(false);
                    }
                });
            }
        } else {
            // No hay usuario autenticado
            android.util.Log.d("MainViewModel", "No hay usuario autenticado");
            _isAuthenticated.setValue(false);
            isAuthenticatedLiveData.setValue(false);
            _isLoading.setValue(false);
            isLoadingLiveData.setValue(false);
        }
    }
    
private void loadCurrentCliente(String userId) {
         android.util.Log.d("MainViewModel", "Intentando cargar cliente con ID: " + userId);
         clienteRepository.getClienteById(Integer.parseInt(userId));
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
    
    public StateFlow<Cliente> getCurrentCliente() {
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
    
    public LiveData<Cliente> getCurrentClienteLiveData() {
        return currentClienteLiveData;
    }
    
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }
}