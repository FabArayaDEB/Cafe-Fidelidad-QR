package com.example.cafefidelidaqrdemo.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import java.util.List;

/**
 * DAO para operaciones CRUD de ProductoEntity
 */
@Dao
public interface ProductoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductoEntity producto);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductoEntity> productos);
    
    @Update
    void update(ProductoEntity producto);
    
    @Delete
    void delete(ProductoEntity producto);
    
    @Delete
    void deleteProducto(ProductoEntity producto);
    
    @Query("SELECT * FROM productos WHERE id_producto = :id")
    ProductoEntity getById(String id);
    
    @Query("SELECT * FROM productos WHERE id_producto = :id")
    ProductoEntity getProductoById(Long id);
    
    @Query("SELECT * FROM productos")
    List<ProductoEntity> getAll();
    
    @Query("SELECT * FROM productos")
    LiveData<List<ProductoEntity>> getAllProductos();
    
    @Query("SELECT * FROM productos")
    List<ProductoEntity> getAllProductosSync();
    
    @Query("SELECT * FROM productos WHERE estado = :estado")
    List<ProductoEntity> getByEstado(String estado);
    
    @Query("SELECT * FROM productos WHERE estado = 'disponible'")
    List<ProductoEntity> getDisponibles();
    
    @Query("SELECT * FROM productos WHERE estado = 'disponible'")
    LiveData<List<ProductoEntity>> getProductosDisponibles();
    
    // Consultas por categoría optimizadas
    @Query("SELECT * FROM productos WHERE categoria = :categoria ORDER BY nombre ASC LIMIT 100")
    List<ProductoEntity> getByCategoria(String categoria);

    @Query("SELECT * FROM productos WHERE categoria = :categoria ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> getProductosByCategoria(String categoria);

    @Query("SELECT * FROM productos WHERE categoria = :categoria AND estado = 'disponible' ORDER BY nombre ASC LIMIT 100")
    List<ProductoEntity> getDisponiblesByCategoria(String categoria);
    
    // Consultas de sincronización optimizadas
    @Query("SELECT * FROM productos WHERE needsSync = 1 ORDER BY nombre ASC LIMIT 100")
    List<ProductoEntity> getPendientesSync();

    @Query("SELECT * FROM productos WHERE synced = 0 ORDER BY nombre ASC LIMIT 100")
    List<ProductoEntity> getNoSincronizados();
    
    @Query("SELECT COUNT(*) FROM productos")
    int getCount();
    
    @Query("SELECT COUNT(*) FROM productos WHERE estado = 'disponible'")
    int getCountDisponibles();
    
    @Query("SELECT COUNT(*) FROM productos WHERE categoria = :categoria")
    int getCountByCategoria(String categoria);
    
    @Query("DELETE FROM productos")
    void deleteAll();
    
    @Query("DELETE FROM productos WHERE id_producto = :id")
    void deleteById(String id);
    
    @Query("UPDATE productos SET needsSync = 1 WHERE id_producto = :id")
    void markForSync(String id);
    
    @Query("UPDATE productos SET synced = 1, needsSync = 0, lastSync = :timestamp WHERE id_producto = :id")
    void markAsSynced(String id, long timestamp);
    
    // Consultas de búsqueda optimizadas con LIMIT
    @Query("SELECT * FROM productos WHERE (nombre LIKE '%' || :query || '%' OR categoria LIKE '%' || :query || '%') AND estado = 'disponible' ORDER BY nombre ASC LIMIT 50")
    List<ProductoEntity> search(String query);

    @Query("SELECT * FROM productos WHERE (nombre LIKE :query OR categoria LIKE :query) AND estado = 'disponible' ORDER BY nombre ASC LIMIT 50")
    List<ProductoEntity> searchProductos(String query);
    
    @Query("SELECT * FROM productos WHERE precio BETWEEN :precioMin AND :precioMax")
    List<ProductoEntity> getByRangoPrecio(double precioMin, double precioMax);
    
    @Query("SELECT * FROM productos WHERE estado = 'disponible' ORDER BY precio ASC")
    List<ProductoEntity> getDisponiblesOrdenadosPorPrecio();
    
    @Query("SELECT * FROM productos WHERE estado = 'disponible' ORDER BY nombre ASC")
    List<ProductoEntity> getDisponiblesOrdenadosPorNombre();
    
    @Query("SELECT DISTINCT categoria FROM productos WHERE estado = 'disponible' ORDER BY categoria ASC")
    List<String> getCategorias();
    
    @Query("SELECT * FROM productos WHERE estado = 'disponible' AND precio <= :precioMaximo ORDER BY precio DESC")
    List<ProductoEntity> getDisponiblesHastaPrecio(double precioMaximo);
    
    @Query("SELECT COUNT(*) FROM productos WHERE nombre = :nombre AND id_producto != :idExcluir")
    int existeProductoConNombre(String nombre, long idExcluir);
    
    @Query("SELECT COUNT(*) FROM productos WHERE codigo = :codigo")
    int existeProductoPorCodigo(String codigo);
    
    @Query("SELECT COUNT(*) FROM productos WHERE nombre = :nombre")
    String existeProductoPorNombre(String nombre);
    
    @Query("SELECT COUNT(*) FROM productos WHERE nombre = :nombre AND id_producto != :idExcluir")
    int existeProductoPorNombreExcluyendoId(String nombre, String idExcluir);
    
    @Query("SELECT COUNT(*) FROM productos WHERE codigo = :codigo AND id_producto != :idExcluir")
    int existeProductoPorCodigoExcluyendoId(String codigo, String idExcluir);
    
    @Query("SELECT COUNT(*) FROM productos WHERE estado = 'disponible'")
    int getCountProductosActivosSync();
    
    @Query("SELECT COUNT(*) FROM productos WHERE estado != 'disponible'")
    int getCountProductosInactivosSync();
}