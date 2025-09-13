package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.UserRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;
import com.example.cafefidelidaqrdemo.utils.SessionManager;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel para gestión del perfil del cliente siguiendo patrones MVVM estrictos
 * Se enfoca únicamente en la preparación de datos para la UI
 */
public class PerfilViewModel extends AndroidViewModel {
    
    // ==================== DEPENDENCIAS ====================
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    // private final FirebaseAuth firebaseAuth;
    
    // ==================== ESTADO DE LA UI ====================
    private final MutableLiveData<String> _userId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isEditing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>(false);
    
    // ==================== DATOS OBSERVABLES ====================
    private final LiveData<UsuarioEntity> usuario;
    private final LiveData<ClienteEntity> cliente;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    
    // ==================== DATOS DERIVADOS ====================
    private final LiveData<Boolean> hasData;
    private final LiveData<Boolean> showEmptyState;
    private final LiveData<Boolean> canEdit;
    private final LiveData<Boolean> canSave;
    private final LiveData<String> statusMessage;
    
    public PerfilViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar dependencias
        userRepository = new UserRepository(application);
        sessionManager = new SessionManager(application);
        // firebaseAuth = FirebaseAuth.getInstance();
        
        // Configurar observables del repositorio
        isLoading = userRepository.getIsLoading();
        error = userRepository.getErrorMessage();
        isOffline = new MutableLiveData<>(false);
        
        // Configurar LiveData reactivo para el usuario
        usuario = Transformations.switchMap(_userId, userId -> {
            if (userId != null && !userId.isEmpty()) {
                return userRepository.getCurrentUser();
            }
            return new MutableLiveData<>(null);
        });
        
        // Configurar LiveData para compatibilidad con ClienteEntity
        cliente = Transformations.map(usuario, this::convertUsuarioToCliente);
        
        // Configurar datos derivados para la UI
        hasData = Transformations.map(usuario, usuarioEntity -> usuarioEntity != null);
        
        showEmptyState = Transformations.map(usuario, usuarioEntity -> {
            Boolean loading = isLoading.getValue();
            return (loading == null || !loading) && usuarioEntity == null;
        });
        
        canEdit = Transformations.map(_isEditing, editing -> {
            Boolean saving = _isSaving.getValue();
            Boolean loading = isLoading.getValue();
            return editing != null && !editing && 
                   (saving == null || !saving) && 
                   (loading == null || !loading);
        });
        
        canSave = Transformations.map(_isEditing, editing -> {
            Boolean saving = _isSaving.getValue();
            Boolean loading = isLoading.getValue();
            return editing != null && editing && 
                   (saving == null || !saving) && 
                   (loading == null || !loading);
        });
        
        statusMessage = Transformations.map(isOffline, offline -> {
            if (offline != null && offline) {
                return "Modo sin conexión - Los cambios se sincronizarán cuando haya conexión";
            }
            return null;
        });
    }
    
    // ==================== OBSERVABLES PARA LA UI ====================
    
    /**
     * Datos del cliente actual
     */
    public LiveData<ClienteEntity> getCliente() {
        return cliente;
    }
    
    /**
     * Datos del cliente actual (alias para compatibilidad)
     */
    public LiveData<ClienteEntity> getClienteData() {
        return cliente;
    }
    
    /**
     * Código QR del cliente
     */
    public LiveData<android.graphics.Bitmap> getClienteQR() {
        MutableLiveData<android.graphics.Bitmap> qrLiveData = new MutableLiveData<>();
        // TODO: Implementar generación de QR cuando sea necesario
        return qrLiveData;
    }
    
    /**
     * Mensaje de éxito
     */
    public LiveData<String> getSuccessMessage() {
        MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();
        Boolean success = _saveSuccess.getValue();
        if (success != null && success) {
            successMessageLiveData.setValue("Perfil actualizado correctamente");
        }
        return successMessageLiveData;
    }
    
    /**
     * Estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Mensajes de error
     */
    public LiveData<String> getError() {
        return error;
    }
    
    /**
     * Estado offline
     */
    public LiveData<Boolean> getIsOffline() {
        return isOffline;
    }
    
    /**
     * Indica si hay datos disponibles
     */
    public LiveData<Boolean> getHasData() {
        return hasData;
    }
    
    /**
     * Indica si mostrar estado vacío
     */
    public LiveData<Boolean> getShowEmptyState() {
        return showEmptyState;
    }
    
    /**
     * Estado de edición
     */
    public LiveData<Boolean> getIsEditing() {
        return _isEditing;
    }
    
    /**
     * Estado de guardado
     */
    public LiveData<Boolean> getIsSaving() {
        return _isSaving;
    }
    
    /**
     * Estado de éxito en guardado
     */
    public LiveData<Boolean> getSaveSuccess() {
        return _saveSuccess;
    }
    
    /**
     * Indica si se puede editar
     */
    public LiveData<Boolean> getCanEdit() {
        return canEdit;
    }
    
    /**
     * Indica si se puede guardar
     */
    public LiveData<Boolean> getCanSave() {
        return canSave;
    }
    
    /**
     * Mensaje de estado para la UI
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * ID del usuario actual
     */
    public LiveData<String> getUserId() {
        return _userId;
    }
    
    /**
     * ID del cliente actual (alias para compatibilidad)
     */
    public LiveData<String> getClienteId() {
        return _userId;
    }
    
    // ==================== ACCIONES DE LA UI ====================
    
    /**
     * Carga los datos del usuario
     */
    public void loadUsuario(String userId) {
        if (userId != null && !userId.isEmpty()) {
            _userId.setValue(userId);
            _isEditing.setValue(false);
            _saveSuccess.setValue(false);
        }
    }
    
    /**
     * Carga los datos del perfil del usuario autenticado
     */
    public void loadPerfilData() {
        // Obtener el ID del usuario autenticado
        String userId = getCurrentUserId();
        if (userId != null && !userId.isEmpty()) {
            loadUsuario(userId);
        } else {
            // No hay usuario autenticado, resetear estado
            _userId.setValue(null);
            _isEditing.setValue(false);
            _saveSuccess.setValue(false);
        }
    }
    
    /**
     * Obtiene el ID del usuario actualmente autenticado
     */
    private String getCurrentUserId() {
        // Primero intentar obtener desde Firebase Auth
        /*
        // FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        */
        
        // Si no está disponible, usar SessionManager como respaldo
        return sessionManager.getUserId();
    }
    
    /**
     * Convierte UsuarioEntity a ClienteEntity para compatibilidad con la UI
     */
    private ClienteEntity convertUsuarioToCliente(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }
        
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId_cliente(usuario.getUid());
        cliente.setNombre(usuario.getNames());
        cliente.setEmail(usuario.getEmail());
        cliente.setTelefono(usuario.getTelefono());
        cliente.setFecha_nac(usuario.getFechaNacimiento());
        cliente.setEstado(usuario.getEstado());
        cliente.setCreado_en(usuario.getDate());
        
        // Los campos específicos de fidelidad se manejan por separado
        
        return cliente;
    }
    
    /**
     * Inicia el modo de edición
     */
    public void startEditing() {
        _isEditing.setValue(true);
        _saveSuccess.setValue(false);
    }
    
    /**
     * Cancela el modo de edición
     */
    public void cancelEditing() {
        _isEditing.setValue(false);
        _saveSuccess.setValue(false);
    }
    
    /**
     * Actualiza los datos del usuario
     */
    public void updateUsuario(UsuarioEntity usuarioEntity) {
        if (usuarioEntity == null) return;
        
        _isSaving.setValue(true);
        userRepository.updateUser(usuarioEntity, new BaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                _isSaving.postValue(false);
                _isEditing.postValue(false);
                _saveSuccess.postValue(true);
            }
            
            @Override
            public void onError(String error) {
                _isSaving.postValue(false);
                // El error se maneja automáticamente por el repositorio
            }
        });
    }
    
    /**
     * Verifica conflictos de versión
     */
    public void checkForConflicts(String userId, ConflictCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("ID de usuario inválido");
            return;
        }
        
        // Para UserRepository, no hay conflictos de versión
        // Los datos se sincronizan automáticamente
        if (callback != null) callback.onResult(false);
    }
    
    /**
     * Fuerza la sincronización
     */
    public void forceSync() {
        // Recargar datos del usuario actual
        String currentUserId = _userId.getValue();
        if (currentUserId != null) {
            loadUsuario(currentUserId);
        }
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        // UserRepository hereda clearError() de BaseRepository
        userRepository.clearError();
    }
    
    /**
     * Resetea el estado de éxito
     */
    public void resetSaveSuccess() {
        _saveSuccess.setValue(false);
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    public void clearSuccessMessage() {
        _saveSuccess.setValue(false);
    }
    
    /**
     * Cierra sesión del usuario
     */
    public void logout() {
        // Limpiar datos del ViewModel
        _userId.setValue(null);
        _isEditing.setValue(false);
        _saveSuccess.setValue(false);
        
        // Cerrar sesión en Firebase y SessionManager
        // firebaseAuth.signOut();
        sessionManager.logout();
    }
    
    /**
     * Refresca los datos del usuario
     */
    public void refreshUsuario() {
        String currentUserId = _userId.getValue();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            // Los datos se actualizan automáticamente a través de LiveData
            // cuando se llama a userRepository.getCurrentUser()
            loadUsuario(currentUserId);
        }
    }
    
    /**
     * Refresca el código QR del cliente
     */
    public void refreshQRCode() {
        // TODO: Implementar regeneración de código QR
    }
    
    // ==================== MÉTODOS DE UTILIDAD ====================
    
    /**
     * Verifica si hay conexión de red
     */
    public boolean hasNetworkConnection() {
        Boolean offline = isOffline.getValue();
        return offline == null || !offline;
    }
    
    /**
     * Verifica si hay datos del cliente
     */
    public boolean hasClienteData() {
        ClienteEntity currentCliente = cliente.getValue();
        return currentCliente != null;
    }
    
    /**
     * Verifica si hay cambios pendientes
     */
    public boolean hasPendingChanges() {
        Boolean editing = _isEditing.getValue();
        return editing != null && editing;
    }
    
    // ==================== INTERFACES ====================
    
    /**
     * Callback para verificación de conflictos
     */
    public interface ConflictCallback {
        void onResult(boolean hasConflicts);
        void onError(String error);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // El repositorio maneja su propia limpieza
    }
}