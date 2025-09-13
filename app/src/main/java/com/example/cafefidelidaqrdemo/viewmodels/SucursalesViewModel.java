package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.SucursalRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel para gestión de sucursales siguiendo patrones MVVM estrictos
 * Se enfoca únicamente en la preparación de datos para la UI
 */
public class SucursalesViewModel extends AndroidViewModel {
    
    // ==================== DEPENDENCIAS ====================
    private final SucursalRepository repository;
    
    // ==================== ESTADO DE LA UI ====================
    private final MutableLiveData<String> _filtroEstado = new MutableLiveData<>("");
    private final MutableLiveData<String> _ordenSeleccionado = new MutableLiveData<>("nombre");
    private final MutableLiveData<Double> _userLatitude = new MutableLiveData<>();
    private final MutableLiveData<Double> _userLongitude = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _locationPermissionDenied = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    
    // ==================== DATOS OBSERVABLES ====================
    private final LiveData<List<SucursalEntity>> sucursales;
    private final LiveData<List<SucursalEntity>> sucursalesActivas;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    
    // ==================== DATOS DERIVADOS ====================
    private final LiveData<Boolean> hasData;
    private final LiveData<Boolean> showEmptyState;
    private final LiveData<Boolean> isLocationDataAvailable;
    private final LiveData<Boolean> canShowDistances;
    private final LiveData<String> statusMessage;
    private final LiveData<Integer> totalSucursales;
    private final LiveData<Integer> sucursalesActivasCount;
    
    public SucursalesViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar repositorio
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        repository = new SucursalRepository(
            database.sucursalDao(),
            ApiService.getInstance(),
            null,
            application
        );
        
        // Configurar observables del repositorio
        sucursales = repository.getAllSucursales();
        sucursalesActivas = repository.getActiveSucursales();
        isLoading = repository.getIsLoading();
        error = repository.getError();
        isOffline = repository.getIsOffline();
        
        // Configurar datos derivados para la UI
        hasData = Transformations.map(sucursales, list -> list != null && !list.isEmpty());
        
        showEmptyState = Transformations.map(sucursales, list -> {
            Boolean loading = isLoading.getValue();
            return (loading == null || !loading) && (list == null || list.isEmpty());
        });
        
        isLocationDataAvailable = Transformations.map(_userLatitude, lat -> {
            Double lon = _userLongitude.getValue();
            return lat != null && lon != null;
        });
        
        canShowDistances = Transformations.map(isLocationDataAvailable, available -> {
            Boolean permissionDenied = _locationPermissionDenied.getValue();
            return available != null && available && (permissionDenied == null || !permissionDenied);
        });
        
        statusMessage = Transformations.map(isOffline, offline -> {
            if (offline != null && offline) {
                return "Modo sin conexión - Mostrando sucursales guardadas";
            }
            return null;
        });
        
        totalSucursales = Transformations.map(sucursales, list -> 
            list != null ? list.size() : 0
        );
        
        sucursalesActivasCount = Transformations.map(sucursalesActivas, list -> 
            list != null ? list.size() : 0
        );
        
        // Cargar datos iniciales
        loadInitialData();
    }
    
    // ==================== OBSERVABLES PARA LA UI ====================
    
    /**
     * Lista completa de sucursales
     */
    public LiveData<List<SucursalEntity>> getSucursales() {
        return sucursales;
    }
    
    /**
     * Lista de sucursales activas
     */
    public LiveData<List<SucursalEntity>> getSucursalesActivas() {
        return sucursalesActivas;
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
     * Filtro de estado actual
     */
    public LiveData<String> getFiltroEstado() {
        return _filtroEstado;
    }
    
    /**
     * Orden seleccionado actual
     */
    public LiveData<String> getOrdenSeleccionado() {
        return _ordenSeleccionado;
    }
    
    /**
     * Latitud del usuario
     */
    public LiveData<Double> getUserLatitude() {
        return _userLatitude;
    }
    
    /**
     * Longitud del usuario
     */
    public LiveData<Double> getUserLongitude() {
        return _userLongitude;
    }
    
    /**
     * Estado de permisos de ubicación
     */
    public LiveData<Boolean> getLocationPermissionDenied() {
        return _locationPermissionDenied;
    }
    
    /**
     * Estado de refresh
     */
    public LiveData<Boolean> getIsRefreshing() {
        return _isRefreshing;
    }
    
    /**
     * Indica si hay datos de ubicación disponibles
     */
    public LiveData<Boolean> getIsLocationDataAvailable() {
        return isLocationDataAvailable;
    }
    
    /**
     * Indica si se pueden mostrar distancias
     */
    public LiveData<Boolean> getCanShowDistances() {
        return canShowDistances;
    }
    
    /**
     * Mensaje de estado para la UI
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Total de sucursales
     */
    public LiveData<Integer> getTotalSucursales() {
        return totalSucursales;
    }
    
    /**
     * Conteo de sucursales activas
     */
    public LiveData<Integer> getSucursalesActivasCount() {
        return sucursalesActivasCount;
    }
    
    // ==================== ACCIONES DE LA UI ====================
    
    /**
     * Carga inicial de datos
     */
    private void loadInitialData() {
        repository.refreshSucursales();
    }
    
    /**
     * Refresca las sucursales desde el servidor
     */
    public void refreshSucursales() {
        _isRefreshing.setValue(true);
        repository.refreshSucursales();
        // El estado de carga se maneja automáticamente por el repositorio
        _isRefreshing.postValue(false);
    }
    
    /**
     * Obtiene una sucursal específica por ID
     */
    public void getSucursalById(long id, SucursalCallback callback) {
        repository.getSucursalById((long)id, new SucursalRepository.SucursalCallback() {
            @Override
            public void onSuccess(Sucursal result) {
                if (callback != null) {
                    // Convertir Sucursal a SucursalEntity si es necesario
                    SucursalEntity entity = convertToEntity(result);
                    callback.onSuccess(entity);
                }
            }
            
            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Busca sucursales por texto
     */
    public void searchSucursales(String query, SearchCallback callback) {
        repository.searchSucursales(query, new SucursalRepository.SearchCallback() {
            @Override
            public void onResults(List<com.example.cafefidelidaqrdemo.models.Sucursal> sucursales) {
                // Convertir List<Sucursal> a List<SucursalEntity>
                List<SucursalEntity> entities = new ArrayList<>();
                for (com.example.cafefidelidaqrdemo.models.Sucursal sucursal : sucursales) {
                    entities.add(convertToEntity(sucursal));
                }
                if (callback != null) callback.onSuccess(entities);
            }
        });
    }
    
    /**
     * Actualiza la ubicación del usuario
     */
    public void updateUserLocation(double latitude, double longitude) {
        _userLatitude.setValue(latitude);
        _userLongitude.setValue(longitude);
    }
    
    /**
     * Obtiene sucursales con distancia calculada
     */
    public void getSucursalesWithDistance(double userLat, double userLon, SucursalesViewModel.DistanceCallback callback) {
        repository.getSucursalesWithDistance(userLat, userLon, new SucursalRepository.DistanceCallback() {
            @Override
            public void onResults(List<SucursalRepository.SucursalWithDistance> sucursales) {
                // Convertir SucursalWithDistance a SucursalEntity para el callback del ViewModel
                List<SucursalEntity> entities = new ArrayList<>();
                for (SucursalRepository.SucursalWithDistance item : sucursales) {
                    entities.add(convertToEntity(item.getSucursal()));
                }
                if (callback != null) callback.onSuccess(entities);
            }
            
            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Establece el estado de permisos de ubicación
     */
    public void setLocationPermissionDenied(boolean denied) {
        _locationPermissionDenied.setValue(denied);
    }
    
    /**
     * Establece filtro por estado
     */
    public void setFiltroEstado(String estado) {
        _filtroEstado.setValue(estado);
    }
    
    /**
     * Establece orden de visualización
     */
    public void setOrdenSeleccionado(String orden) {
        _ordenSeleccionado.setValue(orden);
    }
    
    /**
     * Limpia todos los filtros
     */
    public void clearFilters() {
        _filtroEstado.setValue("");
        _ordenSeleccionado.setValue("nombre");
    }
    
    /**
     * Fuerza sincronización
     */
    public void forceSyncSucursales() {
        repository.forceSyncSucursales();
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        repository.clearError();
    }
    
    /**
     * Reintenta la última operación
     */
    public void retryLastOperation() {
        refreshSucursales();
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
     * Verifica si los datos están vacíos
     */
    public boolean isDataEmpty() {
        List<SucursalEntity> currentData = sucursales.getValue();
        return currentData == null || currentData.isEmpty();
    }
    
    /**
     * Verifica si hay filtros activos
     */
    public boolean hasActiveFilters() {
        String estado = _filtroEstado.getValue();
        String orden = _ordenSeleccionado.getValue();
        return (estado != null && !estado.isEmpty()) || 
               (orden != null && !"nombre".equals(orden));
    }
    
    // ==================== MÉTODOS DE CONVERSIÓN ====================
    
    /**
     * Convierte un objeto Sucursal a SucursalEntity
     */
    private SucursalEntity convertToEntity(com.example.cafefidelidaqrdemo.models.Sucursal sucursal) {
        SucursalEntity entity = new SucursalEntity();
        entity.setId_sucursal(sucursal.getId());
        entity.setNombre(sucursal.getNombre());
        entity.setDireccion(sucursal.getDireccion());
        entity.setLat(sucursal.getLatitud());
        entity.setLon(sucursal.getLongitud());
        entity.setHorario(sucursal.getHorarioApertura() + " - " + sucursal.getHorarioCierre());
        entity.setEstado(sucursal.isActiva() ? "activo" : "inactivo");
        return entity;
    }
    
    // ==================== INTERFACES ====================
    
    /**
     * Callback para operaciones con sucursales individuales
     */
    public interface SucursalCallback {
        void onSuccess(SucursalEntity sucursal);
        void onError(String error);
    }
    
    /**
     * Callback para búsquedas
     */
    public interface SearchCallback {
        void onSuccess(List<SucursalEntity> sucursales);
        void onError(String error);
    }
    
    /**
     * Callback para cálculo de distancias
     */
    public interface DistanceCallback {
        void onSuccess(List<SucursalEntity> sucursales);
        void onError(String error);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // El repositorio maneja su propia limpieza
    }
}