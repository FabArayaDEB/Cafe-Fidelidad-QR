package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.models.ProgresoGeneral;
import com.example.cafefidelidaqrdemo.models.ProximoBeneficio;
import com.example.cafefidelidaqrdemo.models.SyncStatus;
import com.example.cafefidelidaqrdemo.repository.ProgresoRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.network.ApiService;

import java.util.Date;
import java.util.List;

/**
 * ViewModel para gestión del progreso siguiendo patrones MVVM estrictos
 * Se enfoca únicamente en la preparación de datos para la UI
 */
public class ProgresoViewModel extends AndroidViewModel {
    
    // ==================== DEPENDENCIAS ====================
    private final ProgresoRepository repository;
    
    // ==================== ESTADO DE LA UI ====================
    private final MutableLiveData<String> _clienteId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _showEstimated = new MutableLiveData<>(false);
    private final MutableLiveData<Date> _lastSyncTime = new MutableLiveData<>();
    
    // ==================== DATOS OBSERVABLES ====================
    private final LiveData<ProgresoGeneral> progresoGeneral;
    private final LiveData<List<BeneficioEntity>> beneficiosDisponibles;
    private final LiveData<List<ProximoBeneficio>> proximosBeneficios;
    private final LiveData<SyncStatus> syncStatus;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    
    // ==================== DATOS DERIVADOS ====================
    private final LiveData<Boolean> hasData;
    private final LiveData<Boolean> showEmptyState;
    private final LiveData<String> statusMessage;
    private final LiveData<Boolean> needsSync;
    private final LiveData<String> progressSummary;
    private final LiveData<Boolean> canRefresh;
    
    public ProgresoViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar repositorio
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        repository = new ProgresoRepository(
            database.visitaDao(),
            database.beneficioDao(),
            database.canjeDao(),
            ApiService.getInstance()
        );
        
        // Configurar observables del repositorio
        isLoading = repository.getIsLoading();
        error = repository.getError();
        isOffline = repository.getIsOffline();
        
        // Configurar LiveData reactivo para el progreso
        progresoGeneral = Transformations.switchMap(_clienteId, clienteId -> {
            if (clienteId != null && !clienteId.isEmpty()) {
                return repository.getProgresoGeneral(clienteId);
            }
            return new MutableLiveData<>(null);
        });
        
        // Configurar LiveData para beneficios disponibles
        beneficiosDisponibles = Transformations.switchMap(_clienteId, clienteId -> {
            if (clienteId != null && !clienteId.isEmpty()) {
                return repository.getBeneficiosDisponibles(clienteId);
            }
            return new MutableLiveData<>(null);
        });
        
        // Configurar LiveData para próximos beneficios
        proximosBeneficios = Transformations.switchMap(_clienteId, clienteId -> {
            if (clienteId != null && !clienteId.isEmpty()) {
                return repository.getProximosBeneficios(clienteId);
            }
            return new MutableLiveData<>(null);
        });
        
        // Configurar estado de sincronización
        syncStatus = repository.getSyncStatus();
        
        // Configurar datos derivados para la UI
        hasData = Transformations.map(progresoGeneral, progreso -> progreso != null);
        
        showEmptyState = Transformations.map(progresoGeneral, progreso -> {
            Boolean loading = isLoading.getValue();
            Boolean refreshing = _isRefreshing.getValue();
            return (loading == null || !loading) && 
                   (refreshing == null || !refreshing) && 
                   progreso == null;
        });
        
        statusMessage = Transformations.map(isOffline, offline -> {
            if (offline != null && offline) {
                return "Modo sin conexión - Mostrando progreso local";
            }
            Boolean showEst = _showEstimated.getValue();
            if (showEst != null && showEst) {
                return "Datos estimados - Toca para sincronizar";
            }
            return null;
        });
        
        needsSync = Transformations.map(_lastSyncTime, lastSync -> {
            if (lastSync == null) return true;
            long timeDiff = new Date().getTime() - lastSync.getTime();
            return timeDiff > 300000; // 5 minutos
        });
        
        progressSummary = Transformations.map(progresoGeneral, progreso -> {
            if (progreso == null) return "Sin datos de progreso";
            
            int visitasActuales = progreso.getVisitasActuales();
            int visitasObjetivo = progreso.getVisitasObjetivo();
            int porcentaje = progreso.getPorcentajeProgreso();
            
            return String.format("%d de %d visitas (%d%%)", 
                visitasActuales, visitasObjetivo, porcentaje);
        });
        
        canRefresh = Transformations.map(_isRefreshing, refreshing -> {
            Boolean loading = isLoading.getValue();
            return (refreshing == null || !refreshing) && 
                   (loading == null || !loading);
        });
    }
    
    // ==================== OBSERVABLES PARA LA UI ====================
    
    /**
     * Progreso general del cliente
     */
    public LiveData<ProgresoGeneral> getProgresoGeneral() {
        return progresoGeneral;
    }
    
    /**
     * Beneficios disponibles para canjear
     */
    public LiveData<List<BeneficioEntity>> getBeneficiosDisponibles() {
        return beneficiosDisponibles;
    }
    
    /**
     * Próximos beneficios a desbloquear
     */
    public LiveData<List<ProximoBeneficio>> getProximosBeneficios() {
        return proximosBeneficios;
    }
    
    /**
     * Estado de sincronización
     */
    public LiveData<SyncStatus> getSyncStatus() {
        return syncStatus;
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
     * Estado de refresco
     */
    public LiveData<Boolean> getIsRefreshing() {
        return _isRefreshing;
    }
    
    /**
     * Mensaje de estado para la UI
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Indica si necesita sincronización
     */
    public LiveData<Boolean> getNeedsSync() {
        return needsSync;
    }
    
    /**
     * Resumen del progreso
     */
    public LiveData<String> getProgressSummary() {
        return progressSummary;
    }
    
    /**
     * Indica si se puede refrescar
     */
    public LiveData<Boolean> getCanRefresh() {
        return canRefresh;
    }
    
    /**
     * Tiempo de última sincronización
     */
    public LiveData<Date> getLastSyncTime() {
        return _lastSyncTime;
    }
    
    /**
     * Indica si se muestran datos estimados
     */
    public LiveData<Boolean> getShowEstimated() {
        return _showEstimated;
    }
    
    // ==================== ACCIONES DE LA UI ====================
    
    /**
     * Carga los datos de progreso
     */
    public void loadProgresoData(String clienteId) {
        if (clienteId != null && !clienteId.isEmpty()) {
            _clienteId.setValue(clienteId);
            _isRefreshing.setValue(false);
            _showEstimated.setValue(false);
            
            // Verificar si necesita sincronización
            Boolean needsSyncValue = needsSync.getValue();
            if (needsSyncValue != null && needsSyncValue) {
                _showEstimated.setValue(true);
            }
        }
    }
    
    /**
     * Refresca los datos desde el servidor
     */
    public void refreshProgresoData() {
        String currentClienteId = _clienteId.getValue();
        if (currentClienteId != null && !currentClienteId.isEmpty()) {
            _isRefreshing.setValue(true);
            _showEstimated.setValue(false);
            
            repository.refreshProgreso(currentClienteId, new BaseRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    _isRefreshing.postValue(false);
                    _lastSyncTime.postValue(new Date());
                }
                
                @Override
                public void onError(String error) {
                    _isRefreshing.postValue(false);
                    _showEstimated.postValue(true);
                    // El error se maneja automáticamente por el repositorio
                }
            });
        }
    }
    
    /**
     * Fuerza la sincronización completa
     */
    public void forceSync() {
        String currentClienteId = _clienteId.getValue();
        if (currentClienteId != null && !currentClienteId.isEmpty()) {
            _isRefreshing.setValue(true);
            _showEstimated.setValue(false);
            
            repository.forceSyncProgreso(currentClienteId, new BaseRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    _isRefreshing.postValue(false);
                    _lastSyncTime.postValue(new Date());
                }
                
                @Override
                public void onError(String error) {
                    _isRefreshing.postValue(false);
                    _showEstimated.postValue(true);
                    // El error se maneja automáticamente por el repositorio
                }
            });
        }
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        repository.clearError();
    }
    
    /**
     * Marca como datos estimados
     */
    public void markAsEstimated() {
        _showEstimated.setValue(true);
    }
    
    /**
     * Limpia el estado estimado
     */
    public void clearEstimatedState() {
        _showEstimated.setValue(false);
    }
    
    /**
     * Actualiza el tiempo de sincronización
     */
    public void updateSyncTime() {
        _lastSyncTime.setValue(new Date());
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
     * Verifica si hay datos de progreso
     */
    public boolean hasProgresoData() {
        ProgresoGeneral progreso = progresoGeneral.getValue();
        return progreso != null;
    }
    
    /**
     * Verifica si los datos están desactualizados
     */
    public boolean isDataStale() {
        Date lastSync = _lastSyncTime.getValue();
        if (lastSync == null) return true;
        
        long timeDiff = new Date().getTime() - lastSync.getTime();
        return timeDiff > 300000; // 5 minutos
    }
    
    /**
     * Obtiene el porcentaje de progreso actual
     */
    public int getCurrentProgressPercentage() {
        ProgresoGeneral progreso = progresoGeneral.getValue();
        return progreso != null ? progreso.getPorcentajeProgreso() : 0;
    }
    
    /**
     * Obtiene el número de visitas actuales
     */
    public int getCurrentVisitCount() {
        ProgresoGeneral progreso = progresoGeneral.getValue();
        return progreso != null ? progreso.getVisitasActuales() : 0;
    }
    
    /**
     * Obtiene el objetivo de visitas
     */
    public int getVisitGoal() {
        ProgresoGeneral progreso = progresoGeneral.getValue();
        return progreso != null ? progreso.getVisitasObjetivo() : 0;
    }
    
    /**
     * Verifica si hay beneficios disponibles
     */
    public boolean hasBeneficiosDisponibles() {
        List<BeneficioEntity> beneficios = beneficiosDisponibles.getValue();
        return beneficios != null && !beneficios.isEmpty();
    }
    
    /**
     * Obtiene el número de beneficios disponibles
     */
    public int getBeneficiosDisponiblesCount() {
        List<BeneficioEntity> beneficios = beneficiosDisponibles.getValue();
        return beneficios != null ? beneficios.size() : 0;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // El repositorio maneja su propia limpieza
    }
}