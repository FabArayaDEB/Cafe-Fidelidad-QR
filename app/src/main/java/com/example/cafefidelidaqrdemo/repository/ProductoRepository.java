package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.RetrofitClient;
import com.example.cafefidelidaqrdemo.repository.interfaces.IProductoRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para manejar operaciones de Producto con SQLite
 */
public class ProductoRepository implements IProductoRepository {
    
    private static ProductoRepository instance;
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para observar cambios
    private final MutableLiveData<List<Producto>> productosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Producto>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    
    public ProductoRepository(Context context) {
        this.context = context;
        this.database = CafeFidelidadDB.getInstance(context);
        this.apiService = RetrofitClient.getInstance(context).getApiService();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized ProductoRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ProductoRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    public static ProductoRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ProductoRepository must be initialized with context first");
        }
        return instance;
    }
    
    // ========== OPERACIONES CRUD ==========
    
    /**
     * Obtiene todos los productos
     */
    public LiveData<List<Producto>> getAllProductos() {
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerTodosLosProductos();
                productosLiveData.postValue(productos);
            } catch (Exception e) {
                errorLiveData.postValue("Error al obtener productos: " + e.getMessage());
                productosLiveData.postValue(null);
            }
        });
        return productosLiveData;
    }
    
    /**
     * Obtiene productos por categoría
     */
    public LiveData<List<Producto>> getProductosByCategoria(String categoria) {
        MutableLiveData<List<Producto>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerProductosPorCategoria(categoria);
                result.postValue(productos);
            } catch (Exception e) {
                errorLiveData.postValue("Error al obtener productos por categoría: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Obtiene productos disponibles
     */
    public LiveData<List<Producto>> getProductosDisponibles() {
        MutableLiveData<List<Producto>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerTodosLosProductos();
                // Filtrar solo los disponibles
                productos.removeIf(producto -> !producto.isDisponible());
                result.postValue(productos);
            } catch (Exception e) {
                errorLiveData.postValue("Error al obtener productos disponibles: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Obtiene un producto por ID
     */
    public LiveData<Producto> getProductoById(int productoId) {
        MutableLiveData<Producto> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Producto producto = database.obtenerProductoPorId(productoId);
                result.postValue(producto);
            } catch (Exception e) {
                errorLiveData.postValue("Error al obtener producto: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Crea un nuevo producto
     */
    public void createProducto(Producto producto, ProductoCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                // Validar datos del producto
                if (!validarProducto(producto)) {
                    callback.onError("Datos del producto inválidos");
                    isLoadingLiveData.postValue(false);
                    return;
                }
                
                // Insertar producto
                long id = database.insertarProducto(producto);
                if (id > 0) {
                    producto.setId(String.valueOf(id));
                    callback.onSuccess(producto);
                    successLiveData.postValue("Producto creado exitosamente");
                    // Refrescar lista de productos
                    refreshProductosList();
                } else {
                    callback.onError("Error al crear producto");
                }
            } catch (Exception e) {
                callback.onError("Error al crear producto: " + e.getMessage());
                errorLiveData.postValue("Error al crear producto: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza un producto
     */
    public void updateProducto(Producto producto, ProductoCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (!validarProducto(producto)) {
                    callback.onError("Datos del producto inválidos");
                    isLoadingLiveData.postValue(false);
                    return;
                }
                
                int rowsAffected = database.actualizarProducto(producto);
                if (rowsAffected > 0) {
                    callback.onSuccess(producto);
                    successLiveData.postValue("Producto actualizado exitosamente");
                    // Refrescar lista de productos
                    refreshProductosList();
                } else {
                    callback.onError("No se pudo actualizar el producto");
                }
            } catch (Exception e) {
                callback.onError("Error al actualizar producto: " + e.getMessage());
                errorLiveData.postValue("Error al actualizar producto: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Elimina un producto
     */
    public void eliminarProducto(int productoId, ProductoCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int rowsAffected = database.eliminarProducto(productoId);
                if (rowsAffected > 0) {
                    callback.onSuccess(null);
                    successLiveData.postValue("Producto eliminado exitosamente");
                    // Refrescar lista de productos
                    refreshProductosList();
                } else {
                    callback.onError("No se pudo eliminar el producto");
                }
            } catch (Exception e) {
                callback.onError("Error al eliminar producto: " + e.getMessage());
                errorLiveData.postValue("Error al eliminar producto: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    // ========== BÚSQUEDA ==========
    
    /**
     * Busca productos por nombre o descripción (método legacy)
     */
    public void searchProductos(String query, ProductoListCallback callback) {
        searchProductos(query, new BaseRepository.RepositoryCallback<List<Producto>>() {
            @Override
            public void onSuccess(List<Producto> result) {
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Busca productos por categoría y query (método legacy)
     */
    public void searchProductosByCategory(String query, String categoria, ProductoListCallback callback) {
        searchProductosByCategory(query, categoria, null, new BaseRepository.RepositoryCallback<List<Producto>>() {
            @Override
            public void onSuccess(List<Producto> result) {
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene los resultados de búsqueda
     */
    public LiveData<List<Producto>> getSearchResults() {
        return searchResultsLiveData;
    }
    
    /**
     * Limpia los resultados de búsqueda
     */
    public void clearSearchResults() {
        searchResultsLiveData.setValue(null);
    }
    
    // ========== MÉTODOS SÍNCRONOS ==========
    
    /**
     * Obtiene un producto por ID de forma síncrona
     */
    public Producto getProductoByIdSync(int productoId) {
        try {
            return database.obtenerProductoPorId(productoId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtiene todos los productos de forma síncrona
     */
    public List<Producto> getAllProductosSync() {
        try {
            return database.obtenerTodosLosProductos();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtiene productos por categoría de forma síncrona
     */
    public List<Producto> getProductosByCategoriaSync(String categoria) {
        try {
            return database.obtenerProductosPorCategoria(categoria);
        } catch (Exception e) {
            return null;
        }
    }
    
    // ========== VALIDACIÓN ==========
    
    private boolean validarProducto(Producto producto) {
        return producto != null &&
               producto.getNombre() != null && !producto.getNombre().trim().isEmpty() &&
               producto.getDescripcion() != null && !producto.getDescripcion().trim().isEmpty() &&
               producto.getPrecio() > 0 &&
               producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty();
    }
    
    // ========== ESTADÍSTICAS ==========
    
    /**
     * Obtiene el conteo total de productos
     */
    public LiveData<Integer> getCountProductos() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = database.obtenerConteoProductos();
                result.postValue(count);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    /**
     * Obtiene el conteo de productos de forma síncrona
     */
    public int getCountProductosSync() {
        try {
            return database.obtenerConteoProductos();
        } catch (Exception e) {
            return 0;
        }
    }
    
    // ========== UTILIDADES PRIVADAS ==========
    
    /**
     * Refresca la lista de productos internamente
     */
    private void refreshProductosList() {
        try {
            List<Producto> productos = database.obtenerTodosLosProductos();
            productosLiveData.postValue(productos);
        } catch (Exception e) {
            errorLiveData.postValue("Error al refrescar productos: " + e.getMessage());
        }
    }
    
    // ========== GETTERS PARA LIVEDATA ==========
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<String> getSuccess() {
        return successLiveData;
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    // ========== UTILIDADES ==========
    
    /**
     * Limpia los mensajes de error
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    public void clearSuccess() {
        successLiveData.setValue(null);
    }
    
    // ========== MÉTODOS DE LA INTERFAZ ==========
    
    @Override
    public void getProductoById(Long idProducto, BaseRepository.RepositoryCallback<Producto> callback) {
        executor.execute(() -> {
            try {
                Producto producto = database.obtenerProductoPorId(idProducto.intValue());
                if (producto != null) {
                    callback.onSuccess(producto);
                } else {
                    callback.onError("Producto no encontrado");
                }
            } catch (Exception e) {
                callback.onError("Error al obtener producto: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void refreshProductos() {
        refreshProductosList();
    }
    
    @Override
    public void refreshProductos(BaseRepository.SimpleCallback callback) {
        executor.execute(() -> {
            try {
                refreshProductosList();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Error al refrescar productos: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void forceSyncProductos() {
        // Implementación básica - puede expandirse para sincronización con servidor
        refreshProductos();
    }
    
    @Override
    public void forceSyncProductos(BaseRepository.SimpleCallback callback) {
        refreshProductos(callback);
    }
    
    @Override
    public void searchProductos(String query, BaseRepository.RepositoryCallback<List<Producto>> callback) {
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerTodosLosProductos();
                // Filtrar productos que contengan el query en nombre o descripción
                productos.removeIf(producto -> 
                    !producto.getNombre().toLowerCase().contains(query.toLowerCase()) &&
                    !producto.getDescripcion().toLowerCase().contains(query.toLowerCase())
                );
                
                searchResultsLiveData.postValue(productos);
                callback.onSuccess(productos);
            } catch (Exception e) {
                callback.onError("Error en búsqueda: " + e.getMessage());
                errorLiveData.postValue("Error en búsqueda: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void searchProductosByCategory(String query, String categoria, Boolean disponible, 
                                         BaseRepository.RepositoryCallback<List<Producto>> callback) {
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerProductosPorCategoria(categoria);
                
                // Filtrar por disponibilidad si se especifica
                if (disponible != null) {
                    productos.removeIf(producto -> producto.isDisponible() != disponible);
                }
                
                // Filtrar productos que contengan el query
                if (query != null && !query.trim().isEmpty()) {
                    productos.removeIf(producto -> 
                        !producto.getNombre().toLowerCase().contains(query.toLowerCase()) &&
                        !producto.getDescripcion().toLowerCase().contains(query.toLowerCase())
                    );
                }
                
                searchResultsLiveData.postValue(productos);
                callback.onSuccess(productos);
            } catch (Exception e) {
                callback.onError("Error en búsqueda por categoría: " + e.getMessage());
                errorLiveData.postValue("Error en búsqueda por categoría: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void searchProductosRemote(String query, String categoria, 
                                     BaseRepository.RepositoryCallback<List<Producto>> callback) {
        // Implementación básica - busca localmente
        // En una implementación completa, esto haría una llamada al servidor
        searchProductosByCategory(query, categoria, null, callback);
    }
    
    @Override
    public void clearCache(BaseRepository.SimpleCallback callback) {
        executor.execute(() -> {
            try {
                // Limpiar cache en memoria
                productosLiveData.postValue(null);
                searchResultsLiveData.postValue(null);
                clearError();
                clearSuccess();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Error al limpiar cache: " + e.getMessage());
            }
        });
    }
    
    @Override
    public LiveData<Boolean> getIsOffline() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.setValue(!NetworkUtils.isNetworkAvailable(context));
        return result;
    }
    
    // ========== INTERFACES DE CALLBACK ==========
    
    public interface ProductoCallback {
        void onSuccess(Producto producto);
        void onError(String error);
    }
    
    public interface ProductoListCallback {
        void onSuccess(List<Producto> productos);
        void onError(String error);
    }
    
    // ========== MÉTODOS DE CONVERSIÓN ELIMINADOS ==========
    // Los métodos de conversión ya no son necesarios porque usamos directamente los modelos de dominio
    
    // ========== LIMPIEZA ==========
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}