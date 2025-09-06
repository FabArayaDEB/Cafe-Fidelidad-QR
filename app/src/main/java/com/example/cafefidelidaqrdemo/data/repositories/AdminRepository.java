package com.example.cafefidelidaqrdemo.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.BeneficioDao;
import com.example.cafefidelidaqrdemo.data.dao.ProductoDao;
import com.example.cafefidelidaqrdemo.data.dao.SucursalDao;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.data.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.data.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.RetrofitClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository para operaciones administrativas CRUD
 * Maneja productos, beneficios y sucursales
 */
public class AdminRepository {
    
    private final ProductoDao productoDao;
    private final BeneficioDao beneficioDao;
    private final SucursalDao sucursalDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    public AdminRepository(Context context) {
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.productoDao = database.productoDao();
        this.beneficioDao = database.beneficioDao();
        this.sucursalDao = database.sucursalDao();
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    // ========== GESTIÓN DE PRODUCTOS ==========
    
    /**
     * Obtiene todos los productos
     */
    public LiveData<List<ProductoEntity>> getAllProductos() {
        return productoDao.getAllProductos();
    }
    
    /**
     * Obtiene productos activos
     */
    public LiveData<List<ProductoEntity>> getProductosActivos() {
        return productoDao.getProductosActivos();
    }
    
    /**
     * Obtiene un producto por ID
     */
    public LiveData<ProductoEntity> getProductoById(long id) {
        return productoDao.getProductoById(id);
    }
    
    /**
     * Busca productos por nombre
     */
    public LiveData<List<ProductoEntity>> buscarProductos(String nombre) {
        return productoDao.buscarProductosPorNombre(nombre);
    }
    
    /**
     * Obtiene productos con stock bajo
     */
    public LiveData<List<ProductoEntity>> getProductosStockBajo(int umbral) {
        return productoDao.getProductosConStockBajo(umbral);
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public void actualizarStockProducto(long productoId, int nuevoStock, String motivo) {
        executor.execute(() -> {
            try {
                long fechaModificacion = System.currentTimeMillis();
                int filasAfectadas = productoDao.actualizarStock(productoId, nuevoStock, fechaModificacion);
                
                if (filasAfectadas > 0) {
                    // Registrar el cambio en historial si es necesario
                    // historialDao.insertarCambioStock(productoId, nuevoStock, motivo, fechaModificacion);
                }
            } catch (Exception e) {
                // Manejar error
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Actualiza el precio de un producto
     */
    public void actualizarPrecioProducto(long productoId, double nuevoPrecio, String motivo) {
        executor.execute(() -> {
            try {
                // Obtener el producto actual
                ProductoEntity producto = productoDao.getProductoByIdSync(productoId);
                if (producto != null) {
                    producto.setPrecio(nuevoPrecio);
                    productoDao.update(producto);
                    
                    // Registrar el cambio en historial si es necesario
                    // historialDao.insertarCambioPrecio(productoId, nuevoPrecio, motivo, System.currentTimeMillis());
                }
            } catch (Exception e) {
                // Manejar error
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Sincroniza productos con el servidor
     */
    public void sincronizarProductos() {
        executor.execute(() -> {
            try {
                // Simular sincronización con API
                // En una implementación real, aquí se haría la llamada al servidor
                Thread.sleep(2000); // Simular tiempo de sincronización
                
                // Marcar productos como sincronizados
                // List<ProductoEntity> pendientes = productoDao.getNoSincronizados();
                // for (ProductoEntity producto : pendientes) {
                //     productoDao.markAsSynced(producto.getId().toString(), System.currentTimeMillis());
                // }
                
            } catch (Exception e) {
                // Manejar error
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Exporta productos a archivo
     */
    public void exportarProductos() {
        executor.execute(() -> {
            try {
                // Simular exportación de productos
                // En una implementación real, aquí se generaría un archivo CSV o Excel
                Thread.sleep(1500); // Simular tiempo de exportación
                
                // List<ProductoEntity> productos = productoDao.getAllProductosSync();
                // ExportUtils.exportarProductosACSV(productos);
                
            } catch (Exception e) {
                // Manejar error
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Crea un nuevo producto
     */
    public void crearProducto(ProductoEntity producto, AdminCallback<ProductoEntity> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Validar datos del producto
                if (!validarProducto(producto)) {
                    callback.onError("Datos del producto inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar si ya existe un producto con el mismo nombre
                if (productoDao.existeProductoConNombre(producto.getNombre(), 0) > 0) {
                    callback.onError("Ya existe un producto con ese nombre");
                    isLoading.postValue(false);
                    return;
                }
                
                // Insertar en base de datos local
                long id = productoDao.insertProducto(producto);
                producto.setId(id);
                
                // Sincronizar con API
                sincronizarProductoConAPI(producto, "CREATE", new AdminCallback<ProductoEntity>() {
                    @Override
                    public void onSuccess(ProductoEntity result) {
                        callback.onSuccess(result);
                        successMessage.postValue("Producto creado exitosamente");
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Revertir inserción local si falla la API
                        executor.execute(() -> productoDao.deleteProducto(producto));
                        callback.onError("Error al sincronizar con servidor: " + error);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza un producto existente
     */
    public void actualizarProducto(ProductoEntity producto, AdminCallback<ProductoEntity> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Validar datos del producto
                if (!validarProducto(producto)) {
                    callback.onError("Datos del producto inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar versión para control de concurrencia
                int versionActual = productoDao.getVersionProducto(producto.getId());
                if (versionActual != producto.getVersion()) {
                    callback.onError("El producto ha sido modificado por otro usuario. Actualice y vuelva a intentar.");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar nombre único (excluyendo el producto actual)
                if (productoDao.existeProductoConNombre(producto.getNombre(), producto.getId()) > 0) {
                    callback.onError("Ya existe otro producto con ese nombre");
                    isLoading.postValue(false);
                    return;
                }
                
                // Actualizar en base de datos local
                producto.setFechaModificacion(System.currentTimeMillis());
                int rowsUpdated = productoDao.updateProducto(producto);
                
                if (rowsUpdated == 0) {
                    callback.onError("No se pudo actualizar el producto");
                    isLoading.postValue(false);
                    return;
                }
                
                // Sincronizar con API
                sincronizarProductoConAPI(producto, "UPDATE", new AdminCallback<ProductoEntity>() {
                    @Override
                    public void onSuccess(ProductoEntity result) {
                        callback.onSuccess(result);
                        successMessage.postValue("Producto actualizado exitosamente");
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Error al sincronizar con servidor: " + error);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Desactiva un producto (eliminación lógica)
     */
    public void desactivarProducto(long id, String motivo, AdminCallback<Boolean> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si el producto está siendo usado en beneficios activos
                if (beneficioDao.countBeneficiosActivosConProducto(id) > 0) {
                    callback.onError("No se puede desactivar el producto porque está siendo usado en beneficios activos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Desactivar producto
                int rowsUpdated = productoDao.desactivarProducto(id, System.currentTimeMillis(), "admin");
                
                if (rowsUpdated > 0) {
                    // Sincronizar con API
                    sincronizarDesactivacionConAPI("producto", id, motivo, new AdminCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            callback.onSuccess(true);
                            successMessage.postValue("Producto desactivado exitosamente");
                            isLoading.postValue(false);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error al sincronizar con servidor: " + error);
                            isLoading.postValue(false);
                        }
                    });
                } else {
                    callback.onError("No se pudo desactivar el producto");
                    isLoading.postValue(false);
                }
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Activa un producto
     */
    public void activarProducto(long id, AdminCallback<Boolean> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Activar producto
                int rowsUpdated = productoDao.activarProducto(id, System.currentTimeMillis(), "admin");
                
                if (rowsUpdated > 0) {
                    // Sincronizar con API
                    sincronizarActivacionConAPI("producto", id, new AdminCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            callback.onSuccess(true);
                            successMessage.postValue("Producto activado exitosamente");
                            isLoading.postValue(false);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error en sincronización: " + error);
                            isLoading.postValue(false);
                        }
                    });
                } else {
                    callback.onError("No se pudo activar el producto");
                    isLoading.postValue(false);
                }
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== GESTIÓN DE SUCURSALES ==========
    
    /**
     * Obtiene todas las sucursales
     */
    public LiveData<List<SucursalEntity>> getAllSucursales() {
        return sucursalDao.getAllSucursales();
    }
    
    /**
     * Obtiene sucursales activas
     */
    public LiveData<List<SucursalEntity>> getSucursalesActivas() {
        return sucursalDao.getSucursalesActivas();
    }
    
    /**
     * Obtiene una sucursal por ID
     */
    public LiveData<SucursalEntity> getSucursalById(long id) {
        return sucursalDao.getSucursalById(id);
    }
    
    /**
     * Busca sucursales por nombre
     */
    public LiveData<List<SucursalEntity>> buscarSucursales(String nombre) {
        return sucursalDao.buscarSucursalesPorNombre(nombre);
    }
    
    /**
     * Crea una nueva sucursal
     */
    public void crearSucursal(SucursalEntity sucursal, AdminCallback<SucursalEntity> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Validar datos de la sucursal
                if (!validarSucursal(sucursal)) {
                    callback.onError("Datos de la sucursal inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar si ya existe una sucursal en la misma ubicación
                if (sucursalDao.existeSucursalEnUbicacion(sucursal.getLatitud(), sucursal.getLongitud(), 0) > 0) {
                    callback.onError("Ya existe una sucursal en esa ubicación");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar nombre único en la ciudad
                if (sucursalDao.existeSucursalEnCiudad(sucursal.getNombre(), sucursal.getCiudad(), 0) > 0) {
                    callback.onError("Ya existe una sucursal con ese nombre en la ciudad");
                    isLoading.postValue(false);
                    return;
                }
                
                // Insertar en base de datos local
                long id = sucursalDao.insertSucursal(sucursal);
                sucursal.setId(id);
                
                // Sincronizar con API
                sincronizarSucursalConAPI(sucursal, "CREATE", new AdminCallback<SucursalEntity>() {
                    @Override
                    public void onSuccess(SucursalEntity result) {
                        callback.onSuccess(result);
                        successMessage.postValue("Sucursal creada exitosamente");
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Revertir inserción local si falla la API
                        executor.execute(() -> sucursalDao.deleteSucursal(sucursal));
                        callback.onError("Error al sincronizar con servidor: " + error);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza una sucursal existente
     */
    public void actualizarSucursal(SucursalEntity sucursal, AdminCallback<SucursalEntity> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Validar datos de la sucursal
                if (!validarSucursal(sucursal)) {
                    callback.onError("Datos de la sucursal inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar versión para control de concurrencia
                int versionActual = sucursalDao.getVersionSucursal(sucursal.getId());
                if (versionActual != sucursal.getVersion()) {
                    callback.onError("La sucursal ha sido modificada por otro usuario. Actualice y vuelva a intentar.");
                    isLoading.postValue(false);
                    return;
                }
                
                // Actualizar en base de datos local
                sucursal.setFechaModificacion(System.currentTimeMillis());
                int rowsUpdated = sucursalDao.updateSucursal(sucursal);
                
                if (rowsUpdated == 0) {
                    callback.onError("No se pudo actualizar la sucursal");
                    isLoading.postValue(false);
                    return;
                }
                
                // Sincronizar con API
                sincronizarSucursalConAPI(sucursal, "UPDATE", new AdminCallback<SucursalEntity>() {
                    @Override
                    public void onSuccess(SucursalEntity result) {
                        callback.onSuccess(result);
                        successMessage.postValue("Sucursal actualizada exitosamente");
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Error al sincronizar con servidor: " + error);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== GESTIÓN DE BENEFICIOS ==========
    
    /**
     * Obtiene todos los beneficios
     */
    public LiveData<List<BeneficioEntity>> getAllBeneficios() {
        return beneficioDao.getAllBeneficios();
    }
    
    /**
     * Obtiene beneficios activos
     */
    public LiveData<List<BeneficioEntity>> getBeneficiosActivos() {
        return beneficioDao.getBeneficiosActivos();
    }
    
    /**
     * Crea un nuevo beneficio
     */
    public void crearBeneficio(BeneficioEntity beneficio, AdminCallback<BeneficioEntity> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Validar datos del beneficio
                if (!validarBeneficio(beneficio)) {
                    callback.onError("Datos del beneficio inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Insertar en base de datos local
                long id = beneficioDao.insertBeneficio(beneficio);
                beneficio.setId(id);
                
                // Sincronizar con API
                sincronizarBeneficioConAPI(beneficio, "CREATE", new AdminCallback<BeneficioEntity>() {
                    @Override
                    public void onSuccess(BeneficioEntity result) {
                        callback.onSuccess(result);
                        successMessage.postValue("Beneficio creado exitosamente");
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Revertir inserción local si falla la API
                        executor.execute(() -> beneficioDao.deleteBeneficio(beneficio));
                        callback.onError("Error al sincronizar con servidor: " + error);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== MÉTODOS DE VALIDACIÓN ==========
    
    private boolean validarProducto(ProductoEntity producto) {
        return producto != null &&
               producto.getNombre() != null && !producto.getNombre().trim().isEmpty() &&
               producto.getPrecio() >= 0 &&
               producto.getStockDisponible() >= 0;
    }
    
    private boolean validarSucursal(SucursalEntity sucursal) {
        return sucursal != null &&
               sucursal.getNombre() != null && !sucursal.getNombre().trim().isEmpty() &&
               sucursal.getDireccion() != null && !sucursal.getDireccion().trim().isEmpty() &&
               sucursal.getCiudad() != null && !sucursal.getCiudad().trim().isEmpty() &&
               sucursal.getLatitud() >= -90 && sucursal.getLatitud() <= 90 &&
               sucursal.getLongitud() >= -180 && sucursal.getLongitud() <= 180;
    }
    
    private boolean validarBeneficio(BeneficioEntity beneficio) {
        return beneficio != null &&
               beneficio.getNombre() != null && !beneficio.getNombre().trim().isEmpty() &&
               beneficio.getTipo() != null && !beneficio.getTipo().trim().isEmpty() &&
               beneficio.getVigencia_ini() > 0 &&
               beneficio.getVigencia_fin() > beneficio.getVigencia_ini();
    }
    
    // ========== MÉTODOS DE SINCRONIZACIÓN CON API ==========
    
    private void sincronizarProductoConAPI(ProductoEntity producto, String operacion, AdminCallback<ProductoEntity> callback) {
        // Implementar llamadas a la API según la operación
        // Por ahora, simular éxito
        callback.onSuccess(producto);
    }
    
    private void sincronizarSucursalConAPI(SucursalEntity sucursal, String operacion, AdminCallback<SucursalEntity> callback) {
        // Implementar llamadas a la API según la operación
        // Por ahora, simular éxito
        callback.onSuccess(sucursal);
    }
    
    private void sincronizarBeneficioConAPI(BeneficioEntity beneficio, String operacion, AdminCallback<BeneficioEntity> callback) {
        // Implementar llamadas a la API según la operación
        // Por ahora, simular éxito
        callback.onSuccess(beneficio);
    }
    
    private void sincronizarDesactivacionConAPI(String tipo, long id, String motivo, AdminCallback<Boolean> callback) {
        // Implementar llamada a la API para desactivación
        // Por ahora, simular éxito
        callback.onSuccess(true);
    }
    
    private void sincronizarActivacionConAPI(String tipo, long id, AdminCallback<Boolean> callback) {
        // Implementar llamada a la API para activación
        // Por ahora, simular éxito
        callback.onSuccess(true);
    }
    
    public void eliminarProducto(long productoId, AdminCallback<Boolean> callback) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si el producto tiene dependencias
                if (productoTieneDependencias(productoId)) {
                    callback.onError("No se puede eliminar el producto porque tiene beneficios asociados");
                    isLoading.postValue(false);
                    return;
                }
                
                // Eliminar producto de la base de datos local
                int rowsDeleted = productoDao.deleteProductoById(productoId);
                
                if (rowsDeleted > 0) {
                    // Sincronizar con API
                    sincronizarEliminacionConAPI(productoId, new AdminCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            callback.onSuccess(result);
                            successMessage.postValue("Producto eliminado exitosamente");
                            isLoading.postValue(false);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error al sincronizar con servidor: " + error);
                            isLoading.postValue(false);
                        }
                    });
                } else {
                    callback.onError("No se pudo eliminar el producto");
                    isLoading.postValue(false);
                }
                
            } catch (Exception e) {
                callback.onError("Error interno: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public boolean productoTieneDependencias(long productoId) {
        try {
            // Verificar si el producto tiene beneficios asociados
            return beneficioDao.countBeneficiosActivosConProducto(productoId) > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void sincronizarEliminacionConAPI(long productoId, AdminCallback<Boolean> callback) {
        // Implementar llamada a la API para eliminación
        // Por ahora, simular éxito
        callback.onSuccess(true);
    }
    
    public LiveData<List<ProductoEntity>> getProductosPorCategoria(String categoria) {
        return productoDao.getProductosPorCategoria(categoria);
    }
    

    
    // ========== GETTERS PARA OBSERVABLES ==========
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    // ========== ESTADÍSTICAS ==========
    
    public LiveData<Integer> getCountProductosActivos() {
        return productoDao.getCountProductosActivos();
    }
    
    public LiveData<Integer> getCountSucursalesActivas() {
        return sucursalDao.getCountSucursalesActivas();
    }
    
    public LiveData<Integer> getCountBeneficiosActivos() {
        return beneficioDao.getCountBeneficiosActivos();
    }
    
    public LiveData<Integer> getCountProductosInactivos() {
        return productoDao.getCountProductosInactivos();
    }
    
    public LiveData<Integer> getCountSucursalesInactivas() {
        return sucursalDao.getCountSucursalesInactivas();
    }
    
    public LiveData<Integer> getCountBeneficiosInactivos() {
        return beneficioDao.getCountBeneficiosInactivos();
    }
    
    // Métodos síncronos para estadísticas
    public int getCountProductosActivosSync() {
        return productoDao.getCountProductosActivosSync();
    }
    
    public int getCountProductosInactivosSync() {
        return productoDao.getCountProductosInactivosSync();
    }
    
    public int getCountSucursalesActivasSync() {
        return sucursalDao.getCountSucursalesActivasSync();
    }
    
    public int getCountSucursalesInactivasSync() {
        return sucursalDao.getCountSucursalesInactivasSync();
    }
    
    public int getCountBeneficiosActivosSync() {
        return beneficioDao.getCountBeneficiosActivosSync();
    }
    
    public int getCountBeneficiosInactivosSync() {
        return beneficioDao.getCountBeneficiosInactivosSync();
    }
    
    // ========== INTERFACE PARA CALLBACKS ==========
    
    public interface AdminCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    // ========== EXPORTACIÓN ==========
    
    /**
     * Exporta todos los productos a formato CSV
     */
    public void exportarProductos() {
        executor.execute(() -> {
            try {
                List<ProductoEntity> productos = productoDao.getAllProductosSync();
                // Implementar lógica de exportación aquí
                successMessage.postValue("Productos exportados exitosamente");
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar productos: " + e.getMessage());
            }
        });
    }
    
    // ========== LIMPIEZA DE RECURSOS ==========
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}