package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.ProductoRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.List;

/**
 * ViewModel para gestión de productos siguiendo patrones MVVM estrictos
 * Se enfoca únicamente en la preparación de datos para la UI
 */
public class ProductosViewModel extends AndroidViewModel {
    
    // ==================== DEPENDENCIAS ====================
    private final ProductoRepository repository;
    
    // ==================== ESTADO DE LA UI ====================
    private final MutableLiveData<String> _searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> _categoriaFilter = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    
    // ==================== DATOS OBSERVABLES ====================
    private final LiveData<List<ProductoEntity>> productos;
    private final LiveData<List<ProductoEntity>> searchResults;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    
    // ==================== DATOS DERIVADOS ====================
    private final LiveData<Boolean> hasData;
    private final LiveData<Boolean> showEmptyState;
    private final LiveData<String> statusMessage;
    
    public ProductosViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar repositorio
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        repository = new ProductoRepository(
            database.productoDao(),
            ApiService.getInstance()
        );
        
        // Configurar observables del repositorio
        productos = repository.getAllProductos();
        searchResults = repository.getSearchResults();
        isLoading = repository.getIsLoading();
        error = repository.getErrorMessage();
        isOffline = repository.getIsOffline();
        
        // Configurar datos derivados para la UI
        hasData = Transformations.map(productos, list -> list != null && !list.isEmpty());
        
        showEmptyState = Transformations.map(productos, list -> {
            Boolean loading = isLoading.getValue();
            return (loading == null || !loading) && (list == null || list.isEmpty());
        });
        
        statusMessage = Transformations.map(isOffline, offline -> {
            if (offline != null && offline) {
                return "Modo sin conexión - Mostrando datos guardados";
            }
            return null;
        });
        
        // Cargar datos iniciales
        loadInitialData();
    }
    
    // ==================== OBSERVABLES PARA LA UI ====================
    
    /**
     * Lista principal de productos
     */
    public LiveData<List<ProductoEntity>> getProductos() {
        return productos;
    }
    
    /**
     * Resultados de búsqueda
     */
    public LiveData<List<ProductoEntity>> getSearchResults() {
        return searchResults;
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
     * Mensaje de estado para la UI
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Query de búsqueda actual
     */
    public LiveData<String> getSearchQuery() {
        return _searchQuery;
    }
    
    /**
     * Filtro de categoría actual
     */
    public LiveData<String> getCategoriaFilter() {
        return _categoriaFilter;
    }
    
    /**
     * Estado de refresh
     */
    public LiveData<Boolean> getIsRefreshing() {
        return _isRefreshing;
    }
    
    // ==================== ACCIONES DE LA UI ====================
    
    /**
     * Carga inicial de datos
     */
    private void loadInitialData() {
        repository.refreshProductos();
    }
    
    /**
     * Carga productos - método público para ser llamado desde la UI
     */
    public void loadProductos() {
        repository.refreshProductos();
    }
    
    /**
     * Refresca los productos desde el servidor
     */
    public void refreshProductos() {
        _isRefreshing.setValue(true);
        repository.refreshProductos(new BaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                _isRefreshing.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                _isRefreshing.postValue(false);
            }
        });
    }
    
    /**
     * Realiza búsqueda de productos
     */
    public void searchProductos(String query) {
        _searchQuery.setValue(query);
        if (query != null && !query.trim().isEmpty()) {
            repository.searchProductos(query, null);
        } else {
            repository.clearSearchResults();
        }
    }
    
    /**
     * Filtra productos por categoría
     */
    public void filterByCategoria(String categoria) {
        _categoriaFilter.setValue(categoria);
        String currentQuery = _searchQuery.getValue();
        if (currentQuery == null) currentQuery = "";
        
        repository.searchProductosByCategory(currentQuery, categoria, true, null);
    }
    
    /**
     * Obtiene un producto específico por ID
     */
    public void getProductoById(Long idProducto, ProductoCallback callback) {
        repository.getProductoById(idProducto, new BaseRepository.RepositoryCallback<ProductoEntity>() {
            @Override
            public void onSuccess(ProductoEntity result) {
                if (callback != null) callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        repository.clearError();
    }
    
    /**
     * Limpia resultados de búsqueda
     */
    public void clearSearch() {
        _searchQuery.setValue("");
        _categoriaFilter.setValue("");
        repository.clearSearchResults();
    }
    
    /**
     * Fuerza sincronización
     */
    public void forceSyncProductos() {
        repository.forceSyncProductos();
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
        List<ProductoEntity> currentData = productos.getValue();
        return currentData == null || currentData.isEmpty();
    }
    
    /**
     * Obtiene el conteo de productos
     */
    public int getProductosCount() {
        List<ProductoEntity> currentData = productos.getValue();
        return currentData != null ? currentData.size() : 0;
    }
    
    // ==================== INTERFACES ====================
    
    /**
     * Callback para operaciones con productos individuales
     */
    public interface ProductoCallback {
        void onSuccess(ProductoEntity producto);
        void onError(String error);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // El repositorio maneja su propia limpieza
    }
}