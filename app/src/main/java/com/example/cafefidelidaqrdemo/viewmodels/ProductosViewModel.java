package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.network.ApiClient;
import com.example.cafefidelidaqrdemo.repository.ProductoRepository;
import com.example.cafefidelidaqrdemo.sync.SyncManager;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;

import java.util.List;

public class ProductosViewModel extends AndroidViewModel {
    
    private final ProductoRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> categoriaFilter = new MutableLiveData<>("");
    private final MutableLiveData<String> estadoFilter = new MutableLiveData<>("");
    
    // LiveData para la UI
    private final LiveData<List<ProductoEntity>> productos;
    private final LiveData<List<ProductoEntity>> productosDisponibles;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    private final LiveData<Boolean> networkStatus;
    
    public ProductosViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar dependencias
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        
        repository = new ProductoRepository(
            database.productoDao(),
            ApiClient.getApiService()
        );
        
        // Configurar LiveData
        productos = repository.getAllProductos();
        productosDisponibles = repository.getProductosDisponibles();
        isLoading = repository.getIsLoading();
        error = repository.getError();
        isOffline = repository.getIsOffline();
        
        // Observar estado de red
        networkStatus = Transformations.map(isOffline, offline -> !offline);
        
        // Cargar datos iniciales si es necesario
        checkAndLoadInitialData();
    }
    
    // Getters para LiveData
    public LiveData<List<ProductoEntity>> getProductos() {
        return productos;
    }
    
    public LiveData<List<ProductoEntity>> getProductosDisponibles() {
        return productosDisponibles;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getIsOffline() {
        return isOffline;
    }
    
    public LiveData<Boolean> getNetworkStatus() {
        return networkStatus;
    }
    
    // Métodos principales
    public void loadProductos() {
        if (NetworkUtils.isNetworkAvailable()) {
            refreshProductos();
        }
        // Si no hay red, los datos se cargarán automáticamente desde el cache local
    }
    
    public void refreshProductos() {
        repository.refreshProductos();
    }
    
    public void searchProductos(String query) {
        searchQuery.setValue(query);
        if (query != null && !query.trim().isEmpty()) {
            repository.searchProductos(query.trim(), productos -> {
                // Los resultados se manejan a través del callback
                // En una implementación más compleja, podrías usar un LiveData separado para búsquedas
            });
        }
    }
    
    public void filterByCategoria(String categoria) {
        categoriaFilter.setValue(categoria);
        // El filtrado se maneja en el Fragment para mejor rendimiento
    }
    
    public void filterByEstado(String estado) {
        estadoFilter.setValue(estado);
        // El filtrado se maneja en el Fragment para mejor rendimiento
    }
    
    public void getProductoById(Long idProducto, ProductoCallback callback) {
        repository.getProductoById(idProducto, new ProductoRepository.ProductoCallback() {
            @Override
            public void onSuccess(Producto producto) {
                callback.onSuccess(producto);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    public void forceSyncProductos() {
        repository.forceSyncProductos();
    }
    
    public void clearError() {
        repository.clearError();
    }
    
    public void retryLastOperation() {
        if (NetworkUtils.isNetworkAvailable()) {
            refreshProductos();
        } else {
            // Mostrar mensaje de que no hay conexión
            // Esto se maneja en el Fragment
        }
    }
    
    // Métodos de utilidad
    private void checkAndLoadInitialData() {
        // Verificar si hay datos en cache
        if (productos.getValue() == null || productos.getValue().isEmpty()) {
            if (NetworkUtils.isNetworkAvailable()) {
                refreshProductos();
            }
        }
    }
    
    public boolean hasNetworkConnection() {
        return NetworkUtils.isNetworkAvailable();
    }
    
    public boolean isDataEmpty() {
        List<ProductoEntity> currentProductos = productos.getValue();
        return currentProductos == null || currentProductos.isEmpty();
    }
    
    public int getProductosCount() {
        List<ProductoEntity> currentProductos = productos.getValue();
        return currentProductos != null ? currentProductos.size() : 0;
    }
    
    public int getProductosDisponiblesCount() {
        List<ProductoEntity> currentProductos = productosDisponibles.getValue();
        return currentProductos != null ? currentProductos.size() : 0;
    }
    
    // Métodos para obtener estadísticas
    public void getProductosStats(StatsCallback callback) {
        if (productos.getValue() != null) {
            List<ProductoEntity> allProductos = productos.getValue();
            int total = allProductos.size();
            int disponibles = 0;
            int noDisponibles = 0;
            
            for (ProductoEntity producto : allProductos) {
                if ("activo".equalsIgnoreCase(producto.getEstado())) {
                    disponibles++;
                } else {
                    noDisponibles++;
                }
            }
            
            callback.onStats(total, disponibles, noDisponibles);
        } else {
            callback.onStats(0, 0, 0);
        }
    }
    
    // Interfaces de callback
    public interface ProductoCallback {
        void onSuccess(Producto producto);
        void onError(String error);
    }
    
    public interface StatsCallback {
        void onStats(int total, int disponibles, int noDisponibles);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar recursos si es necesario
    }
}