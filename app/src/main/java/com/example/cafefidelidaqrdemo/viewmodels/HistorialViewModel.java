package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.models.HistorialItem;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.ApiClient;
import com.example.cafefidelidaqrdemo.repository.HistorialRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.Date;
import java.util.List;

/**
 * ViewModel para gestión del historial siguiendo patrones MVVM estrictos
 * Se enfoca únicamente en la preparación de datos para la UI
 */
public class HistorialViewModel extends AndroidViewModel {
    
    // ==================== CONSTANTES ====================
    public static final int PAGE_SIZE = 20;
    
    // ==================== DEPENDENCIAS ====================
    private final HistorialRepository repository;
    
    // ==================== ESTADO DE LA UI ====================
    private final MutableLiveData<String> _clienteId = new MutableLiveData<>();
    private final MutableLiveData<String> clienteId = new MutableLiveData<>();
    private final MutableLiveData<FiltroTipo> _filtroTipo = new MutableLiveData<>(FiltroTipo.TODOS);
    private final MutableLiveData<FiltroEstado> _filtroEstado = new MutableLiveData<>(FiltroEstado.TODOS);
    private final MutableLiveData<Date> _fechaInicio = new MutableLiveData<>();
    private final MutableLiveData<Date> _fechaFin = new MutableLiveData<>();
    private final MutableLiveData<Integer> _currentPage = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    
    // ==================== DATOS OBSERVABLES ====================
    private final LiveData<List<HistorialItem>> historialItems;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    
    // ==================== DATOS DERIVADOS ====================
    private final LiveData<Boolean> hasData;
    private final LiveData<Boolean> showEmptyState;
    private final LiveData<Boolean> canLoadMore;
    private final LiveData<String> statusMessage;
    private final LiveData<String> filtroSummary;
    
    // ==================== ENUMS ====================
    
    public enum FiltroTipo {
        TODOS("Todos"),
        VISITAS("Visitas"),
        CANJES("Canjes");
        
        private final String displayName;
        
        FiltroTipo(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum FiltroEstado {
        TODOS("Todos"),
        PENDIENTES("Pendientes"),
        ENVIADOS("Enviados");
        
        private final String displayName;
        
        FiltroEstado(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public HistorialViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar repositorio
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        repository = new HistorialRepository(
            database.visitaDao(),
            database.canjeDao(),
            ApiClient.getApiService()
        );
        
        // Configurar observables del repositorio
        isLoading = repository.getIsLoading();
        error = repository.getError();
        isOffline = repository.getIsOffline();
        
        // Configurar LiveData reactivo para el historial
        historialItems = Transformations.switchMap(_clienteId, clienteId -> {
            if (clienteId != null && !clienteId.isEmpty()) {
                FiltroTipo tipo = _filtroTipo.getValue();
                FiltroEstado estado = _filtroEstado.getValue();
                Date fechaInicio = _fechaInicio.getValue();
                Date fechaFin = _fechaFin.getValue();
                Integer page = _currentPage.getValue();
                
                return repository.getHistorialItems(
                    clienteId,
                    tipo != null ? tipo : FiltroTipo.TODOS,
                    estado != null ? estado : FiltroEstado.TODOS,
                    fechaInicio,
                    fechaFin,
                    page != null ? page : 0,
                    PAGE_SIZE
                );
            }
            return new MutableLiveData<>(null);
        });
        
        // Configurar datos derivados para la UI
        hasData = Transformations.map(historialItems, items -> 
            items != null && !items.isEmpty()
        );
        
        showEmptyState = Transformations.map(historialItems, items -> {
            Boolean loading = isLoading.getValue();
            Boolean refreshing = _isRefreshing.getValue();
            return (loading == null || !loading) && 
                   (refreshing == null || !refreshing) && 
                   (items == null || items.isEmpty());
        });
        
        canLoadMore = Transformations.map(historialItems, items -> {
            Boolean loading = isLoading.getValue();
            Boolean loadingMore = _isLoadingMore.getValue();
            return (loading == null || !loading) && 
                   (loadingMore == null || !loadingMore) && 
                   items != null && items.size() >= PAGE_SIZE;
        });
        
        statusMessage = Transformations.map(isOffline, offline -> {
            if (offline != null && offline) {
                return "Modo sin conexión - Mostrando datos locales";
            }
            return null;
        });
        
        filtroSummary = Transformations.map(_filtroTipo, tipo -> {
            FiltroEstado estado = _filtroEstado.getValue();
            Date fechaInicio = _fechaInicio.getValue();
            Date fechaFin = _fechaFin.getValue();
            
            StringBuilder summary = new StringBuilder();
            
            if (tipo != null && tipo != FiltroTipo.TODOS) {
                summary.append(tipo.getDisplayName());
            }
            
            if (estado != null && estado != FiltroEstado.TODOS) {
                if (summary.length() > 0) summary.append(" • ");
                summary.append(estado.getDisplayName());
            }
            
            if (fechaInicio != null || fechaFin != null) {
                if (summary.length() > 0) summary.append(" • ");
                summary.append("Con filtro de fecha");
            }
            
            return summary.length() > 0 ? summary.toString() : "Sin filtros";
        });
    }
    
    // ==================== OBSERVABLES PARA LA UI ====================
    
    /**
     * Lista de elementos del historial
     */
    public LiveData<List<HistorialItem>> getHistorialItems() {
        return historialItems;
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
     * Estado de carga de más elementos
     */
    public LiveData<Boolean> getIsLoadingMore() {
        return _isLoadingMore;
    }
    
    /**
     * Indica si se pueden cargar más elementos
     */
    public LiveData<Boolean> getCanLoadMore() {
        return canLoadMore;
    }
    
    /**
     * Mensaje de estado para la UI
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Resumen de filtros aplicados
     */
    public LiveData<String> getFiltroSummary() {
        return filtroSummary;
    }
    
    /**
     * Filtro de tipo actual
     */
    public LiveData<FiltroTipo> getFiltroTipo() {
        return _filtroTipo;
    }
    
    /**
     * Filtro de estado actual
     */
    public LiveData<FiltroEstado> getFiltroEstado() {
        return _filtroEstado;
    }
    
    /**
     * Fecha de inicio del filtro
     */
    public LiveData<Date> getFechaInicio() {
        return _fechaInicio;
    }
    
    /**
     * Fecha de fin del filtro
     */
    public LiveData<Date> getFechaFin() {
        return _fechaFin;
    }
    
    /**
     * Página actual
     */
    public LiveData<Integer> getCurrentPage() {
        return _currentPage;
    }
    
    /**
     * Estado de sincronización
     */
    public LiveData<Boolean> getSyncStatus() {
        return repository.getIsLoading();
    }
    
    /**
     * Estado de la red
     */
    public LiveData<Boolean> getNetworkStatus() {
        return repository.getIsOffline();
    }
    
    // ==================== ACCIONES DE LA UI ====================
    
    /**
     * Aplica filtros al historial
     */
    public void applyFilters(FiltroTipo filtroTipo, FiltroEstado filtroEstado, Date fechaInicio, Date fechaFin) {
        _filtroTipo.setValue(filtroTipo);
        _filtroEstado.setValue(filtroEstado);
        _fechaInicio.setValue(fechaInicio);
        _fechaFin.setValue(fechaFin);
        _currentPage.setValue(1);
        
        // Recargar historial con nuevos filtros
        String clienteId = this.clienteId.getValue();
        if (clienteId != null) {
            loadHistorial(clienteId);
        }
    }
    
    /**
     * Carga el historial inicial
     */
    public void loadHistorial(String clienteId) {
        if (clienteId != null && !clienteId.isEmpty()) {
            _clienteId.setValue(clienteId);
            _currentPage.setValue(0);
            _isRefreshing.setValue(false);
            _isLoadingMore.setValue(false);
        }
    }
    
    /**
     * Refresca el historial desde el servidor
     */
    public void refreshHistorial(String clienteId) {
        _clienteId.setValue(clienteId);
        refreshHistorial();
    }
    
    /**
     * Refresca el historial desde el servidor
     */
    public void refreshHistorial() {
        String currentClienteId = _clienteId.getValue();
        if (currentClienteId != null && !currentClienteId.isEmpty()) {
            _isRefreshing.setValue(true);
            _currentPage.setValue(0);
            
            repository.refreshHistorial(currentClienteId, new BaseRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    _isRefreshing.postValue(false);
                }
                
                @Override
                public void onError(String error) {
                    _isRefreshing.postValue(false);
                    // El error se maneja automáticamente por el repositorio
                }
            });
        }
    }
    
    /**
     * Carga más elementos para paginación
     */
    public void loadMoreHistorial(String clienteId) {
        _clienteId.setValue(clienteId);
        loadMoreHistorial();
    }
    
    /**
     * Carga más elementos para paginación
     */
    public void loadMoreHistorial() {
        Boolean canLoad = canLoadMore.getValue();
        if (canLoad != null && canLoad) {
            _isLoadingMore.setValue(true);
            Integer currentPageValue = _currentPage.getValue();
            int nextPage = (currentPageValue != null ? currentPageValue : 0) + 1;
            _currentPage.setValue(nextPage);
            
            // Simular delay para mostrar loading
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                _isLoadingMore.setValue(false);
            }, 500);
        }
    }
    
    /**
     * Aplica filtro de tipo
     */
    public void setFiltroTipo(FiltroTipo tipo) {
        if (tipo != null) {
            _filtroTipo.setValue(tipo);
            _currentPage.setValue(0);
        }
    }
    
    /**
     * Aplica filtro de estado
     */
    public void setFiltroEstado(FiltroEstado estado) {
        if (estado != null) {
            _filtroEstado.setValue(estado);
            _currentPage.setValue(0);
        }
    }
    
    /**
     * Aplica filtro de fechas
     */
    public void setFiltroFechas(Date fechaInicio, Date fechaFin) {
        _fechaInicio.setValue(fechaInicio);
        _fechaFin.setValue(fechaFin);
        _currentPage.setValue(0);
    }
    
    /**
     * Limpia todos los filtros
     */
    public void clearFilters() {
        _filtroTipo.setValue(FiltroTipo.TODOS);
        _filtroEstado.setValue(FiltroEstado.TODOS);
        _fechaInicio.setValue(null);
        _fechaFin.setValue(null);
        _currentPage.setValue(0);
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        repository.clearError();
    }
    
    /**
     * Fuerza la sincronización
     */
    public void forceSync() {
        String currentClienteId = _clienteId.getValue();
        if (currentClienteId != null && !currentClienteId.isEmpty()) {
            repository.syncPendingData(new BaseRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    // Recargar datos después de la sincronización
                    refreshHistorial();
                }
                
                @Override
                public void onError(String error) {
                    // El error se maneja automáticamente por el repositorio
                }
            });
        }
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
     * Verifica si hay datos del historial
     */
    public boolean hasHistorialData() {
        List<HistorialItem> items = historialItems.getValue();
        return items != null && !items.isEmpty();
    }
    
    /**
     * Verifica si hay filtros aplicados
     */
    public boolean hasActiveFilters() {
        FiltroTipo tipo = _filtroTipo.getValue();
        FiltroEstado estado = _filtroEstado.getValue();
        Date fechaInicio = _fechaInicio.getValue();
        Date fechaFin = _fechaFin.getValue();
        
        return (tipo != null && tipo != FiltroTipo.TODOS) ||
               (estado != null && estado != FiltroEstado.TODOS) ||
               fechaInicio != null || fechaFin != null;
    }
    
    /**
     * Obtiene el número total de elementos
     */
    public int getTotalItemCount() {
        List<HistorialItem> items = historialItems.getValue();
        return items != null ? items.size() : 0;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // El repositorio maneja su propia limpieza
    }
}