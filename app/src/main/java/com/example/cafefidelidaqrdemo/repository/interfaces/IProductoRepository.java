package com.example.cafefidelidaqrdemo.repository.interfaces;

import androidx.lifecycle.LiveData;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.List;

/**
 * Interfaz para el repositorio de productos
 * Define el contrato para operaciones CRUD y búsqueda de productos
 */
public interface IProductoRepository {
    
    //OBSERVABLES
    
    /**
     * LiveData con todos los productos
     */
    LiveData<List<ProductoEntity>> getAllProductos();
    
    /**
     * LiveData con resultados de búsqueda
     */
    LiveData<List<ProductoEntity>> getSearchResults();
    
    /**
     * Estado de carga
     */
    LiveData<Boolean> getIsLoading();
    
    /**
     * Estado de error
     */
    LiveData<String> getError();
    
    /**
     * Estado offline
     */
    LiveData<Boolean> getIsOffline();
    
    //OPERACIONES CRUD
    
    /**
     * Obtiene un producto por ID
     */
    void getProductoById(Long idProducto, BaseRepository.RepositoryCallback<ProductoEntity> callback);
    
    /**
     * Actualiza la lista de productos desde el servidor
     */
    void refreshProductos();
    
    /**
     * Actualiza la lista de productos con callback
     */
    void refreshProductos(BaseRepository.SimpleCallback callback);
    
    /**
     * Fuerza la sincronización de productos
     */
    void forceSyncProductos();
    
    /**
     * Fuerza la sincronización con callback
     */
    void forceSyncProductos(BaseRepository.SimpleCallback callback);
    
    // BÚSQUEDA
    
    /**
     * Busca productos localmente
     */
    void searchProductos(String query, BaseRepository.RepositoryCallback<List<ProductoEntity>> callback);
    
    /**
     * Busca productos por categoría
     */
    void searchProductosByCategory(String query, String categoria, Boolean disponible, 
                                   BaseRepository.RepositoryCallback<List<ProductoEntity>> callback);
    
    /**
     * Busca productos remotamente
     */
    void searchProductosRemote(String query, String categoria, 
                              BaseRepository.RepositoryCallback<List<ProductoEntity>> callback);
    
    // UTILIDADES
    
    /**
     * Limpia los resultados de búsqueda
     */
    void clearSearchResults();
    
    /**
     * Limpia errores
     */
    void clearError();
    
    /**
     * Limpia cache local
     */
    void clearCache(BaseRepository.SimpleCallback callback);
}