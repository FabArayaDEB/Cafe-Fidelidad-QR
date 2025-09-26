package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.database.models.Sucursal;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.SucursalRepository;

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
    private final LiveData<List<com.example.cafefidelidaqrdemo.database.models.Sucursal>> sucursales;
    private final LiveData<List<com.example.cafefidelidaqrdemo.database.models.Sucursal>> sucursalesActivas;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    
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
        repository = new SucursalRepository(application);
        
        // Configurar observables del repositorio
        sucursales = repository.getAllSucursales();
        sucursalesActivas = repository.getAllSucursales();
        isLoading = repository.getIsLoading();
        error = repository.getError();

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

        statusMessage = Transformations.map(sucursales, list -> {
            if (list == null || list.isEmpty()) {
                return "No hay sucursales disponibles en este momento.";
            }
            return "Se encontraron " + list.size() + " sucursales.";
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
    public LiveData<List<com.example.cafefidelidaqrdemo.database.models.Sucursal>> getSucursales() {
        return sucursales;
    }
    
    /**
     * Lista de sucursales activas
     */
    public LiveData<List<Sucursal>> getSucursalesActivas() {
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
        repository.refreshSucursales(result -> {
            // Callback para carga inicial - no necesita acción específica
        });
    }
    
    /**
     * Refresca las sucursales desde el servidor
     */
    public void refreshSucursales() {
        _isRefreshing.setValue(true);
        repository.refreshSucursales(result -> {
            // El estado de carga se maneja automáticamente por el repositorio
            _isRefreshing.postValue(false);
        });
    }
    
    /**
     * Obtiene una sucursal específica por ID
     */
    public void getSucursalById(long id, SucursalCallback callback) {
        repository.getSucursalById((long)id, new SucursalRepository.SucursalCallback() {
            @Override
            public void onSuccess(Sucursal result) {
                if (callback != null) {
                    callback.onSuccess(result);
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
                if (callback != null) callback.onSuccess(sucursales);
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
                // Extraer las sucursales de los objetos SucursalWithDistance
                List<Sucursal> sucursalesList = new ArrayList<>();
                for (SucursalRepository.SucursalWithDistance item : sucursales) {
                    sucursalesList.add(item.getSucursal());
                }
                if (callback != null) callback.onSuccess(sucursalesList);
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
        // Simplificado - asume conexión disponible
        return true;
    }
    
    /**
     * Verifica si los datos están vacíos
     */
    public boolean isDataEmpty() {
        List<Sucursal> currentData = sucursales.getValue();
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
    
    // ==================== INTERFACES ====================
    
    /**
     * Callback para operaciones con sucursales individuales
     */
    public interface SucursalCallback {
        void onSuccess(Sucursal sucursal);
        void onError(String error);
    }
    
    /**
     * Callback para búsquedas
     */
    public interface SearchCallback {
        void onSuccess(List<Sucursal> sucursales);
        void onError(String error);
    }
    
    /**
     * Callback para cálculo de distancias
     */
    public interface DistanceCallback {
        void onSuccess(List<Sucursal> sucursales);
        void onError(String error);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // El repositorio maneja su propia limpieza
    }
}