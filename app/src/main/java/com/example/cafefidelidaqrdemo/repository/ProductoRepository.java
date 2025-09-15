package com.example.cafefidelidaqrdemo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.dao.ProductoDao;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;
import com.example.cafefidelidaqrdemo.repository.interfaces.IProductoRepository;

import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import com.example.cafefidelidaqrdemo.utils.SearchManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository para gestión de productos siguiendo arquitectura MVVM
 * Maneja cache local, sincronización y búsquedas
 * Implementa IProductoRepository
 */
public class ProductoRepository extends BaseRepository implements IProductoRepository {
    private final ProductoDao productoDao;
    private final ApiService apiService;
    private final SearchManager searchManager;
    
    // LiveData específicos de productos
    private final MutableLiveData<List<ProductoEntity>> _searchResults = new MutableLiveData<>();
    
    public ProductoRepository(ProductoDao productoDao, ApiService apiService) {
        super(2); // Pool de 2 threads para operaciones de productos
        this.productoDao = productoDao;
        this.apiService = apiService;
        this.searchManager = new SearchManager();
    }
    
    //GETTERS PARA LIVEDATA
    
    public LiveData<List<ProductoEntity>> getAllProductos() {
        return productoDao.getAllProductos();
    }
    
    public LiveData<List<ProductoEntity>> getProductosByCategoria(String categoria) {
        return productoDao.getProductosByCategoria(categoria);
    }
    
    public LiveData<List<ProductoEntity>> getProductosDisponibles() {
        return productoDao.getProductosDisponibles();
    }
    
    public LiveData<List<ProductoEntity>> getSearchResults() {
        return _searchResults;
    }
    
    // OPERACIONES PRINCIPALES
    
    /**
     * Refresca la lista de productos desde la API
     */
    public void refreshProductos() {
        refreshProductos(null);
    }
    
    /**
     * Refresca la lista de productos con callback
     */
    public void refreshProductos(SimpleCallback callback) {
        if (!NetworkUtils.isNetworkAvailable()) {
            setOffline(true);
            if (callback != null) callback.onError("Sin conexión a internet");
            return;
        }
        
        setLoading(true);
        setOffline(false);
        clearError();
        
        apiService.getProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Producto> productos = response.body();
                    executeInBackground(() -> {
                        try {
                            // Convertir y guardar en cache local
                            List<ProductoEntity> entities = new ArrayList<>();
                            for (Producto producto : productos) {
                                entities.add(convertToEntity(producto));
                            }
                            
                            // Limpiar cache anterior y insertar nuevos datos
                            productoDao.deleteAll();
                            productoDao.insertAll(entities);
                            
                            setSuccess("Productos actualizados exitosamente");
                            if (callback != null) callback.onSuccess();
                        } catch (Exception e) {
                            String error = "Error al guardar productos: " + e.getMessage();
                            setError(error);
                            if (callback != null) callback.onError(error);
                        }
                    });
                } else {
                    String error = "Error al cargar productos: " + response.message();
                    setError(error);
                    if (callback != null) callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                setError(error);
                setOffline(true);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene un producto por ID con callback
     */
    public void getProductoById(Long idProducto, RepositoryCallback<ProductoEntity> callback) {
        executeInBackground(() -> {
            try {
                ProductoEntity entity = productoDao.getProductoById(idProducto);
                if (entity != null) {
                    if (callback != null) callback.onSuccess(entity);
                } else {
                    // Si no está en cache, buscar en API
                    fetchProductoFromApi(idProducto, callback);
                }
            } catch (Exception e) {
                String error = "Error al obtener producto: " + e.getMessage();
                setError(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    private void fetchProductoFromApi(Long idProducto, RepositoryCallback<ProductoEntity> callback) {
        if (!NetworkUtils.isNetworkAvailable()) {
            setOffline(true);
            if (callback != null) callback.onError("Sin conexión a internet");
            return;
        }
        
        setLoading(true);
        apiService.getProductoById(String.valueOf(idProducto)).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Producto producto = response.body();
                    ProductoEntity entity = convertToEntity(producto);
                    // Guardar en cache
                    executeInBackground(() -> {
                        try {
                            productoDao.insert(entity);
                        } catch (Exception e) {
                            // Log error but don't fail the operation
                        }
                    });
                    if (callback != null) callback.onSuccess(entity);
                } else {
                    String error = "Producto no encontrado";
                    setError(error);
                    if (callback != null) callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                setLoading(false);
                String error = "Error de conexión: " + t.getMessage();
                setError(error);
                setOffline(true);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Busca productos localmente
     */
    public void searchProductos(String query, RepositoryCallback<List<ProductoEntity>> callback) {
        executeInBackground(() -> {
            try {
                List<ProductoEntity> allProductos = productoDao.getAllProductosSync();
                // TODO: Implementar searchProductosLocal en SearchManager
                // List<ProductoEntity> results = searchManager.searchProductosLocal(allProductos, query, null, null);
                List<ProductoEntity> results = allProductos; // Temporal: devolver todos los productos
                _searchResults.postValue(results);
                if (callback != null) callback.onSuccess(results);
            } catch (Exception e) {
                String error = "Error en búsqueda: " + e.getMessage();
                setError(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Busca productos por categoría
     */
    public void searchProductosByCategory(String query, String categoria, Boolean disponible, RepositoryCallback<List<ProductoEntity>> callback) {
        executeInBackground(() -> {
            try {
                List<ProductoEntity> allProductos = productoDao.getAllProductosSync();
                // TODO: Implementar searchProductosLocal en SearchManager
                // List<ProductoEntity> results = searchManager.searchProductosLocal(allProductos, query, categoria, disponible);
                List<ProductoEntity> results = allProductos; // Temporal: devolver todos los productos
                _searchResults.postValue(results);
                if (callback != null) callback.onSuccess(results);
            } catch (Exception e) {
                String error = "Error en búsqueda por categoría: " + e.getMessage();
                setError(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Busca productos remotamente
     */
    public void searchProductosRemote(String query, String categoria, RepositoryCallback<List<ProductoEntity>> callback) {
        if (!NetworkUtils.isNetworkAvailable()) {
            setOffline(true);
            // Fallback a búsqueda local
            searchProductosByCategory(query, categoria, true, callback);
            return;
        }
        
        setLoading(true);
        // Por ahora, usar búsqueda local como fallback
        // TODO: Implementar búsqueda remota real cuando esté disponible la API
        searchProductosByCategory(query, categoria, true, new RepositoryCallback<List<ProductoEntity>>() {
            @Override
            public void onSuccess(List<ProductoEntity> result) {
                setLoading(false);
                if (callback != null) callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                setLoading(false);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Fuerza la sincronización de productos
     */
    public void forceSyncProductos() {
        forceSyncProductos(null);
    }
    
    /**
     * Fuerza la sincronización de productos con callback
     */
    public void forceSyncProductos(SimpleCallback callback) {
        refreshProductos(callback);
    }
    
    /**
     * Limpia los resultados de búsqueda
     */
    public void clearSearchResults() {
        _searchResults.postValue(new ArrayList<>());
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
    
    // Los métodos de conversión ya están definidos arriba, eliminando duplicados
    
    /**
     * Estado de error (implementación de IProductoRepository)
     */
    @Override
    public LiveData<String> getError() {
        return getErrorMessage();
    }
    
    /**
     * Limpia cache local
     */
    @Override
    public void clearCache(SimpleCallback callback) {
        executeInBackground(() -> {
            try {
                productoDao.deleteAll();
                _searchResults.postValue(new ArrayList<>());
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                String error = "Error al limpiar cache: " + e.getMessage();
                setError(error);
                if (callback != null) callback.onError(error);
            }
        });
    }
}