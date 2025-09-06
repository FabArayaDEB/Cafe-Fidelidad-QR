package com.example.cafefidelidaqrdemo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.dao.ProductoDao;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.network.ApiService;

import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import com.example.cafefidelidaqrdemo.utils.SearchManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoRepository {
    private final ProductoDao productoDao;
    private final ApiService apiService;
    private final SearchManager searchManager;
    private final Executor executor;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);
    
    public ProductoRepository(ProductoDao productoDao, ApiService apiService) {
        this.productoDao = productoDao;
        this.apiService = apiService;
        this.searchManager = new SearchManager();
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // LiveData getters
    public LiveData<List<ProductoEntity>> getAllProductos() {
        return productoDao.getAllProductos();
    }
    
    public LiveData<List<ProductoEntity>> getProductosByCategoria(String categoria) {
        return productoDao.getProductosByCategoria(categoria);
    }
    
    public LiveData<List<ProductoEntity>> getProductosDisponibles() {
        return productoDao.getProductosDisponibles();
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
    
    // Métodos principales
    public void refreshProductos() {
        if (!NetworkUtils.isNetworkAvailable()) {
            isOffline.postValue(true);
            return;
        }
        
        isLoading.postValue(true);
        isOffline.postValue(false);
        error.postValue(null);
        
        apiService.getProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                isLoading.postValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Producto> productos = response.body();
                    executor.execute(() -> {
                        // Convertir y guardar en cache local
                        List<ProductoEntity> entities = new ArrayList<>();
                        for (Producto producto : productos) {
                            entities.add(convertToEntity(producto));
                        }
                        
                        // Limpiar cache anterior y insertar nuevos datos
                        productoDao.deleteAll();
                        productoDao.insertAll(entities);
                    });
                } else {
                    error.postValue("Error al cargar productos: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    public void getProductoById(Long idProducto, ProductoCallback callback) {
        executor.execute(() -> {
            ProductoEntity entity = productoDao.getProductoById(idProducto);
            if (entity != null) {
                callback.onSuccess(convertToModel(entity));
            } else {
                // Si no está en cache, intentar obtener de API
                if (NetworkUtils.isNetworkAvailable()) {
                    fetchProductoFromApi(idProducto, callback);
                } else {
                    callback.onError("Producto no encontrado y sin conexión");
                }
            }
        });
    }
    
    private void fetchProductoFromApi(Long idProducto, ProductoCallback callback) {
        apiService.getProductoById(String.valueOf(idProducto)).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Producto producto = response.body();
                    executor.execute(() -> {
                        // Guardar en cache
                        ProductoEntity entity = convertToEntity(producto);
                        productoDao.insert(entity);
                    });
                    callback.onSuccess(producto);
                } else {
                    callback.onError("Error al obtener producto: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    public void searchProductos(String query, SearchCallback callback) {
        executor.execute(() -> {
            List<ProductoEntity> allProductos = productoDao.getAllProductosSync();
            // TODO: Implementar searchProductosLocal en SearchManager
            // List<ProductoEntity> results = searchManager.searchProductosLocal(allProductos, query, null, null);
            List<ProductoEntity> results = allProductos; // Temporal: devolver todos los productos
            List<Producto> productos = new ArrayList<>();
            for (ProductoEntity entity : results) {
                productos.add(convertToModel(entity));
            }
            callback.onResults(productos);
        });
    }
    
    public void searchProductosByCategory(String query, String categoria, Boolean disponible, SearchCallback callback) {
        executor.execute(() -> {
            List<ProductoEntity> allProductos = productoDao.getAllProductosSync();
            // TODO: Implementar searchProductosLocal en SearchManager
            // List<ProductoEntity> results = searchManager.searchProductosLocal(allProductos, query, categoria, disponible);
            List<ProductoEntity> results = allProductos; // Temporal: devolver todos los productos
            List<Producto> productos = new ArrayList<>();
            for (ProductoEntity entity : results) {
                productos.add(convertToModel(entity));
            }
            callback.onResults(productos);
        });
    }
    
    public void searchProductosRemote(String query, String categoria, RemoteSearchCallback callback) {
        if (!NetworkUtils.isNetworkAvailable()) {
            callback.onError("Sin conexión a internet");
            return;
        }
        
        // Implementar búsqueda remota cuando esté disponible en la API
        // Por ahora, usar búsqueda local como fallback
        searchProductosByCategory(query, categoria, null, new SearchCallback() {
            @Override
            public void onResults(List<Producto> productos) {
                callback.onResults(productos, false); // false = no hay más resultados remotos
            }
        });
    }
    
    public void forceSyncProductos() {
        // SyncManager solo tiene métodos estáticos, no necesita instancia
        // Se puede implementar sincronización específica aquí si es necesario
    }
    
    public void clearError() {
        error.postValue(null);
    }
    
    // Métodos de conversión
    private ProductoEntity convertToEntity(Producto producto) {
        ProductoEntity entity = new ProductoEntity();
        entity.setId_producto(producto.getId());
        entity.setNombre(producto.getNombre());
        entity.setCategoria(producto.getCategoria());
        entity.setPrecio(producto.getPrecio());
        entity.setEstado(producto.getEstado());
        return entity;
    }
    
    private Producto convertToModel(ProductoEntity entity) {
        Producto producto = new Producto();
        producto.setId(entity.getId_producto());
        producto.setNombre(entity.getNombre());
        producto.setCategoria(entity.getCategoria());
        producto.setPrecio(entity.getPrecio());
        producto.setEstado(entity.getEstado());
        return producto;
    }
    
    // Interfaces de callback
    public interface ProductoCallback {
        void onSuccess(Producto producto);
        void onError(String error);
    }
    
    public interface SearchCallback {
        void onResults(List<Producto> productos);
    }
    
    public interface RemoteSearchCallback {
        void onResults(List<Producto> productos, boolean hasMore);
        void onError(String error);
    }
}