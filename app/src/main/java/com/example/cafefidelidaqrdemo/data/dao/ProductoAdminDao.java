package com.example.cafefidelidaqrdemo.data.dao;

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
 * DAO para operaciones CRUD de productos
 * Utilizado en el módulo de administración
 */
@Dao
public interface ProductoAdminDao {
    
    // ========== OPERACIONES BÁSICAS CRUD ==========
    
    /**
     * Inserta un nuevo producto
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProducto(ProductoEntity producto);
    
    /**
     * Inserta múltiples productos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertProductos(List<ProductoEntity> productos);
    
    /**
     * Actualiza un producto existente
     */
    @Update
    int updateProducto(ProductoEntity producto);
    
    /**
     * Elimina un producto (eliminación física - usar con precaución)
     */
    @Delete
    int deleteProducto(ProductoEntity producto);
    
    // ========== CONSULTAS DE LECTURA ==========
    
    /**
     * Obtiene todos los productos (activos e inactivos)
     */
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> getAllProductos();
    
    /**
     * Obtiene todos los productos de forma síncrona
     */
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    List<ProductoEntity> getAllProductosSync();
    
    /**
     * Obtiene solo productos activos
     */
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> getProductosActivos();
    
    /**
     * Obtiene solo productos inactivos
     */
    @Query("SELECT * FROM productos WHERE activo = 0 ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> getProductosInactivos();
    
    /**
     * Obtiene un producto por ID
     */
    @Query("SELECT * FROM productos WHERE id = :id")
    LiveData<ProductoEntity> getProductoById(long id);
    
    /**
     * Obtiene un producto por ID de forma síncrona
     */
    @Query("SELECT * FROM productos WHERE id = :id")
    ProductoEntity getProductoByIdSync(long id);
    
    /**
     * Obtiene un producto por código de barras
     */
    @Query("SELECT * FROM productos WHERE codigo_barras = :codigoBarras")
    ProductoEntity getProductoByCodigoBarras(String codigoBarras);
    
    // ========== CONSULTAS DE BÚSQUEDA ==========
    
    /**
     * Busca productos por nombre (búsqueda parcial)
     */
    @Query("SELECT * FROM productos WHERE nombre LIKE '%' || :nombre || '%' AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> buscarProductosPorNombre(String nombre);
    
    /**
     * Busca productos por categoría
     */
    @Query("SELECT * FROM productos WHERE categoria = :categoria AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> getProductosPorCategoria(String categoria);
    
    /**
     * Obtiene todas las categorías únicas
     */
    @Query("SELECT DISTINCT categoria FROM productos WHERE activo = 1 ORDER BY categoria ASC")
    LiveData<List<String>> getCategorias();
    
    /**
     * Busca productos con stock bajo (menor al mínimo especificado)
     */
    @Query("SELECT * FROM productos WHERE stock_disponible < :stockMinimo AND activo = 1 ORDER BY stock_disponible ASC")
    LiveData<List<ProductoEntity>> getProductosConStockBajo(int stockMinimo);
    
    /**
     * Busca productos sin stock
     */
    @Query("SELECT * FROM productos WHERE stock_disponible = 0 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<ProductoEntity>> getProductosSinStock();
    
    // ========== OPERACIONES ADMINISTRATIVAS ==========
    
    /**
     * Desactiva un producto (eliminación lógica)
     */
    @Query("UPDATE productos SET activo = 0, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int desactivarProducto(long id, long fechaModificacion, String modificadoPor);
    
    /**
     * Activa un producto
     */
    @Query("UPDATE productos SET activo = 1, fecha_modificacion = :fechaModificacion, modificado_por = :modificadoPor, version = version + 1 WHERE id = :id")
    int activarProducto(long id, long fechaModificacion, String modificadoPor);
    
    /**
     * Actualiza el stock de un producto
     */
    @Query("UPDATE productos SET stock_disponible = :nuevoStock, fecha_modificacion = :fechaModificacion, version = version + 1 WHERE id = :id")
    int actualizarStock(long id, int nuevoStock, long fechaModificacion);
    
    /**
     * Verifica si existe un producto con el mismo código de barras (para validación)
     */
    @Query("SELECT COUNT(*) FROM productos WHERE codigo_barras = :codigoBarras AND id != :excludeId")
    int existeCodigoBarras(String codigoBarras, long excludeId);
    
    /**
     * Verifica si existe un producto con el mismo nombre (para validación)
     */
    @Query("SELECT COUNT(*) FROM productos WHERE LOWER(nombre) = LOWER(:nombre) AND id != :excludeId")
    int existeNombreProducto(String nombre, long excludeId);
    
    // ========== CONTROL DE VERSIONES ==========
    
    /**
     * Obtiene la versión actual de un producto (para control de edición simultánea)
     */
    @Query("SELECT version FROM productos WHERE id = :id")
    int getVersionProducto(long id);
    
    /**
     * Actualiza un producto solo si la versión coincide (control de concurrencia optimista)
     */
    @Query("UPDATE productos SET nombre = :nombre, descripcion = :descripcion, precio = :precio, " +
           "categoria = :categoria, imagen_url = :imagenUrl, codigo_barras = :codigoBarras, " +
           "stock_disponible = :stock, fecha_modificacion = :fechaModificacion, " +
           "modificado_por = :modificadoPor, version = version + 1 " +
           "WHERE id = :id AND version = :versionEsperada")
    int updateProductoConVersion(long id, String nombre, String descripcion, double precio,
                                String categoria, String imagenUrl, String codigoBarras,
                                int stock, long fechaModificacion, String modificadoPor,
                                int versionEsperada);
    
    // ========== ESTADÍSTICAS Y REPORTES ==========
    
    /**
     * Cuenta total de productos activos
     */
    @Query("SELECT COUNT(*) FROM productos WHERE activo = 1")
    LiveData<Integer> getCountProductosActivos();
    
    /**
     * Cuenta total de productos inactivos
     */
    @Query("SELECT COUNT(*) FROM productos WHERE activo = 0")
    LiveData<Integer> getCountProductosInactivos();
    
    /**
     * Cuenta total de productos activos (síncrono)
     */
    @Query("SELECT COUNT(*) FROM productos WHERE activo = 1")
    int getCountProductosActivosSync();
    
    /**
     * Cuenta total de productos inactivos (síncrono)
     */
    @Query("SELECT COUNT(*) FROM productos WHERE activo = 0")
    int getCountProductosInactivosSync();
    
    /**
     * Obtiene productos creados en un rango de fechas
     */
    @Query("SELECT * FROM productos WHERE fecha_creacion BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha_creacion DESC")
    LiveData<List<ProductoEntity>> getProductosPorFechaCreacion(long fechaInicio, long fechaFin);
    
    /**
     * Obtiene productos modificados recientemente
     */
    @Query("SELECT * FROM productos WHERE fecha_modificacion > :fechaLimite ORDER BY fecha_modificacion DESC LIMIT :limite")
    LiveData<List<ProductoEntity>> getProductosModificadosRecientemente(long fechaLimite, int limite);
    
    /**
     * Obtiene el valor total del inventario (productos activos)
     */
    @Query("SELECT SUM(precio * stock_disponible) FROM productos WHERE activo = 1")
    LiveData<Double> getValorTotalInventario();
    
    // ========== OPERACIONES DE LIMPIEZA ==========
    
    /**
     * Elimina productos inactivos antiguos (limpieza de base de datos)
     */
    @Query("DELETE FROM productos WHERE activo = 0 AND fecha_modificacion < :fechaLimite")
    int eliminarProductosInactivosAntiguos(long fechaLimite);
    
    /**
     * Elimina todos los productos (usar solo en desarrollo/testing)
     */
    @Query("DELETE FROM productos")
    int eliminarTodosLosProductos();
    
    /**
     * Elimina un producto por ID (eliminación física)
     */
    @Query("DELETE FROM productos WHERE id = :id")
    int deleteProductoById(long id);
    
    /**
     * Verifica si existe un producto con el nombre dado, excluyendo un ID específico
     */
    @Query("SELECT COUNT(*) FROM productos WHERE nombre = :nombre AND id != :idExcluir")
    int existeProductoConNombre(String nombre, long idExcluir);
}