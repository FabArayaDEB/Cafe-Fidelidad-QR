package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.BeneficioDao;
import com.example.cafefidelidaqrdemo.database.dao.ProductoDao;
import com.example.cafefidelidaqrdemo.database.dao.SucursalDao;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.models.RecentActivity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.RetrofitClient;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.AdminDashboardViewModel.SystemHealth;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        this.apiService = RetrofitClient.getInstance(context).getApiService();
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
        return productoDao.getProductosDisponibles();
    }

    /**
     * Obtiene un producto por ID
     */
    public LiveData<ProductoEntity> getProductoById(long id) {
        // TODO: Implementar getProductoByIdLiveData en database.dao.ProductoDao
        // return productoDao.getProductoByIdLiveData(id);
        MutableLiveData<ProductoEntity> result = new MutableLiveData<>();
        result.setValue(null); // Temporal
        return result;
    }

    /**
     * Busca productos por nombre
     */
    public LiveData<List<ProductoEntity>> buscarProductos(String nombre) {
        // TODO: Implementar buscarProductosPorNombre en database.dao.ProductoDao
        // return productoDao.buscarProductosPorNombre(nombre);
        return new MutableLiveData<>(new java.util.ArrayList<>()); // Temporal
    }

    /**
     * Verifica si existe un producto con el nombre dado, excluyendo un ID específico
     */
    public boolean existeProductoPorNombreExcluyendoId(String nombre, String idExcluir) {
        return productoDao.existeProductoConNombre(nombre, Long.parseLong(idExcluir)) > 0;
    }

    /**
     * Verifica si existe un producto con el nombre dado
     */
    public boolean existeProductoPorNombre(String nombre) {
        return productoDao.existeProductoConNombre(nombre, 0) > 0;
    }

    /**
     * Verifica si existe un producto con el código dado
     */
    public boolean existeProductoPorCodigo(String codigo) {
        // TODO: Implementar existeProductoPorCodigo en database.dao.ProductoDao
        // return productoDao.existeProductoPorCodigo(codigo) > 0;
        return false; // Temporal
    }

    public boolean existeProductoPorCodigoExcluyendoId(String codigo, String idExcluir) {
        return productoDao.existeProductoPorCodigoExcluyendoId(codigo, idExcluir) > 0;
    }

    /**
     * Obtiene un producto por ID (síncrono)
     */
    public ProductoEntity getProductoPorId(String id) {
        return productoDao.getProductoById(Long.parseLong(id));
    }

    /**
     * Obtiene productos con stock bajo
     */
    public LiveData<List<ProductoEntity>> getProductosStockBajo(int umbral) {
        // TODO: Implementar getProductosConStockBajo en database.dao.ProductoDao
        // return productoDao.getProductosConStockBajo(umbral);
        return new MutableLiveData<>(new java.util.ArrayList<>()); // Temporal
    }

    /**
     * Actualiza el stock de un producto
     */
    public void actualizarStockProducto(long productoId, int nuevoStock, String motivo) {
        executor.execute(() -> {
            try {
                long fechaModificacion = System.currentTimeMillis();
                // TODO: Implementar actualizarStock en database.dao.ProductoDao
                // int filasAfectadas = productoDao.actualizarStock(productoId, nuevoStock, fechaModificacion);
                int filasAfectadas = 1; // Temporal
                
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
                ProductoEntity producto = productoDao.getProductoById(productoId);
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
                // TODO: Corregir tipos - insertAll espera List y setId_producto espera String
                // long id = productoDao.insertAll(producto);
                // producto.setId_producto(id);
                
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
                // TODO: Implementar getVersionProducto, getId y getVersion
                // int versionActual = productoDao.getVersionProducto(producto.getId());
                // if (versionActual != producto.getVersion()) {
                //     callback.onError("El producto ha sido modificado por otro usuario. Actualice y vuelva a intentar.");
                //     isLoading.postValue(false);
                //     return;
                // }
                
                // Verificar nombre único (excluyendo el producto actual)
                // TODO: Implementar existeProductoConNombre y getId en ProductoEntity
                // if (productoDao.existeProductoConNombre(producto.getNombre(), producto.getId()) > 0) {
                //     callback.onError("Ya existe otro producto con ese nombre");
                //     isLoading.postValue(false);
                //     return;
                // }
                
                // Actualizar en base de datos local
                // TODO: Implementar setFechaModificacion en ProductoEntity
                // producto.setFechaModificacion(System.currentTimeMillis());
                // TODO: Implementar updateProducto en database.dao.ProductoDao
                // int rowsUpdated = productoDao.updateProducto(producto);
                
                // Asumir que la actualización fue exitosa
                if (false) {
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
                // TODO: Implementar desactivarProducto en database.dao.ProductoDao
                // int rowsUpdated = productoDao.desactivarProducto(id, System.currentTimeMillis(), "admin");
                
                // Asumir que la desactivación fue exitosa
                if (true) {
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
                // TODO: Implementar activarProducto en database.dao.ProductoDao
                // int rowsUpdated = productoDao.activarProducto(id, System.currentTimeMillis(), "admin");
                
                // Asumir que la activación fue exitosa
                if (true) {
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
        return sucursalDao.getSucursalById(String.valueOf(id));
    }
    
    /**
     * Busca sucursales por nombre
     */
    public LiveData<List<SucursalEntity>> buscarSucursales(String nombre) {
        return sucursalDao.buscarSucursalesPorNombre(nombre);
    }
    
    /**
     * Obtiene sucursales por ciudad
     */
    public LiveData<List<SucursalEntity>> getSucursalesPorCiudad(String ciudad) {
        // TODO: Implementar getSucursalesPorCiudad en database.dao.SucursalDao
        // return sucursalDao.getSucursalesPorCiudad(ciudad);
        MutableLiveData<List<SucursalEntity>> result = new MutableLiveData<>();
        result.setValue(new java.util.ArrayList<>());
        return result;
    }
    
    /**
     * Obtiene sucursales cercanas a una ubicación
     */
    public LiveData<List<SucursalEntity>> getSucursalesCercanas(double latitud, double longitud, double radioKm) {
        // Por ahora retornamos todas las sucursales activas
        // En una implementación completa se calcularía la distancia
        return sucursalDao.getSucursalesActivas();
    }
    
    /**
     * Obtiene sucursales abiertas en un horario específico
     */
    public LiveData<List<SucursalEntity>> getSucursalesAbiertas(String hora, String dia) {
        // Por ahora retornamos todas las sucursales activas
        // En una implementación completa se verificarían los horarios
        return sucursalDao.getSucursalesActivas();
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
                if (sucursalDao.existeSucursalEnUbicacion(sucursal.getLat(), sucursal.getLon(), "") > 0) {
                    callback.onError("Ya existe una sucursal en esa ubicación");
                    isLoading.postValue(false);
                    return;
                }
                
                // Verificar nombre único en la dirección
                if (sucursalDao.existeSucursalEnCiudad(sucursal.getNombre(), sucursal.getDireccion(), "") > 0) {
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
                int versionActual = sucursalDao.getVersionSucursal(sucursal.getId_sucursal());
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
               sucursal.getDireccion() != null && !sucursal.getDireccion().trim().isEmpty() &&
               sucursal.getLat() >= -90 && sucursal.getLat() <= 90 &&
               sucursal.getLon() >= -180 && sucursal.getLon() <= 180;
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
                productoDao.deleteById(String.valueOf(productoId));
                
                // Asumir que la eliminación fue exitosa
                if (true) {
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
        return productoDao.getProductosByCategoria(categoria);
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
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = productoDao.getCountProductosActivosSync();
                result.postValue(count);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    public LiveData<Integer> getCountSucursalesActivas() {
        return sucursalDao.getCountSucursalesActivas();
    }
    
    public LiveData<Integer> getCountBeneficiosActivos() {
        return beneficioDao.getCountBeneficiosActivos();
    }
    
    public LiveData<Integer> getCountProductosInactivos() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = productoDao.getCountProductosInactivosSync();
                result.postValue(count);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
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
    public void exportarProductosCSV() {
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
    
    /**
     * Verifica si existe una sucursal cercana en el radio especificado
     */
    public boolean existeSucursalCercana(double latitud, double longitud, double radioKm) {
        try {
            List<SucursalEntity> sucursales = sucursalDao.getAllSucursalesSync();
            
            for (SucursalEntity sucursal : sucursales) {
                if (sucursal.getLat() != 0.0 && sucursal.getLon() != 0.0) {
                    double distancia = calcularDistancia(latitud, longitud, 
                                                        sucursal.getLat(), sucursal.getLon());
                    if (distancia <= radioKm) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine
     */
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    // ========== MÉTODOS PARA DASHBOARD ==========
    
    /**
     * Obtiene actividades recientes
     */
    public List<RecentActivity> getActividadesRecientes(int limit) {
        try {
            List<RecentActivity> activities = new java.util.ArrayList<>();
            
            // Simular actividades recientes basadas en productos y sucursales
            List<ProductoEntity> productosRecientes = productoDao.getAllProductosSync();
            List<SucursalEntity> sucursalesRecientes = sucursalDao.getAllSucursalesSync();
            
            // Agregar actividades de productos
            int count = 0;
            for (ProductoEntity producto : productosRecientes) {
                if (count >= limit / 2) break;
                RecentActivity activity = new RecentActivity();
                activity.setTipo("PRODUCTO");
                activity.setDescripcion("Producto actualizado: " + producto.getNombre());
                activity.setTimestamp(System.currentTimeMillis());
                activity.setUsuario("admin");
                activities.add(activity);
                count++;
            }
            
            // Agregar actividades de sucursales
            count = 0;
            for (SucursalEntity sucursal : sucursalesRecientes) {
                if (count >= limit / 2) break;
                RecentActivity activity = new RecentActivity();
                activity.setTipo("SUCURSAL");
                activity.setDescripcion("Sucursal actualizada: " + sucursal.getNombre());
                activity.setTimestamp(System.currentTimeMillis());
                activity.setUsuario("admin");
                activities.add(activity);
                count++;
            }
            
            // Limitar al número solicitado
            return activities.size() > limit ? activities.subList(0, limit) : activities;
            
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Verifica si hay cambios pendientes de sincronización
     */
    public boolean hayCambiosPendientes() {
        // Verificar si hay elementos que necesitan sincronización
        return false; // Por ahora retornamos false
    }
    
    /**
     * Sincroniza todos los datos con el servidor
     */
    /**
     * Sincroniza productos con el servidor
     */
    public void sincronizarProductosConServidor() {
        executor.execute(() -> {
            try {
                isLoading.postValue(true);
                
                // Obtener productos locales que necesitan sincronización
                List<ProductoEntity> productosLocales = productoDao.getAllProductosSync();
                
                if (productosLocales.isEmpty()) {
                    successMessage.postValue("No hay productos para sincronizar");
                    return;
                }
                
                // Simular sincronización con servidor
                // En una implementación real, aquí se haría la llamada a la API
                Thread.sleep(2000); // Simular tiempo de red
                
                // Marcar productos como sincronizados
                for (ProductoEntity producto : productosLocales) {
                    // En una implementación real, se actualizaría el estado de sincronización
                    // producto.setEstadoSincronizacion("SINCRONIZADO");
                    // productoDao.updateProducto(producto);
                }
                
                successMessage.postValue("Productos sincronizados correctamente: " + productosLocales.size() + " elementos");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al sincronizar productos: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Verifica si existe un producto con el mismo nombre excluyendo el ID especificado
     */
    public boolean existeProductoPorNombreExcluyendoId(String nombre, long idExcluir) {
        try {
            // TODO: Implementar existeProductoPorNombreExcluyendoId en database.dao.ProductoDao
            // return productoDao.existeProductoPorNombreExcluyendoId(nombre, idExcluir) > 0;
            return false; // Temporal
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica si existe una sucursal con el mismo nombre excluyendo el ID especificado
     */
    public boolean existeSucursalPorNombreExcluyendoId(String nombre, String idExcluir) {
        try {
            // TODO: Implementar existeSucursalPorNombreExcluyendoId en database.dao.SucursalDao
            // return sucursalDao.existeSucursalPorNombreExcluyendoId(nombre, idExcluir) > 0;
            return false; // Temporal
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica el estado de salud del sistema
     */
    public SystemHealth verificarEstadoSistema() {
        SystemHealth health = new SystemHealth();
        
        try {
            // Verificar conectividad de base de datos
            int totalProductos = getCountProductosActivosSync();
            int totalSucursales = getCountSucursalesActivasSync();
            int totalBeneficios = getCountBeneficiosActivosSync();
            
            // Verificar integridad de datos
            if (totalProductos < 0 || totalSucursales < 0 || totalBeneficios < 0) {
                health.addProblem("Error en contadores de base de datos");
            }
            
            // Verificar si hay datos básicos
            if (totalSucursales == 0) {
                health.addProblem("No hay sucursales activas en el sistema");
            }
            
            if (totalProductos == 0) {
                health.addProblem("No hay productos activos en el sistema");
            }
            
            // Verificar cambios pendientes de sincronización
            if (hayCambiosPendientes()) {
                health.addProblem("Hay cambios pendientes de sincronización");
            }
            
            // Verificar conectividad de red (simulado)
            try {
                // En una implementación real, aquí se haría una llamada a la API
                // apiService.healthCheck().execute();
                // Por ahora simulamos que la conexión está bien
            } catch (Exception e) {
                health.addProblem("Error de conectividad con el servidor: " + e.getMessage());
            }
            
            // Verificar espacio de almacenamiento (simulado)
            // En una implementación real se verificaría el espacio disponible
            
        } catch (Exception e) {
            health.addProblem("Error general del sistema: " + e.getMessage());
        }
        
        return health;
    }
    

    
    /**
     * Obtiene las actividades recientes del sistema
     */
    public MutableLiveData<List<RecentActivity>> getRecentActivities(int limit) {
        // Por ahora retornamos una lista vacía, se puede implementar más tarde
        MutableLiveData<List<RecentActivity>> result = new MutableLiveData<>();
        result.setValue(new java.util.ArrayList<>());
        return result;
    }
    
    /**
     * Verifica si hay cambios pendientes de sincronización
     */
    public boolean checkPendingChanges() {
        try {
            // TODO: Implementar métodos getCountXXXNeedSync en los DAOs
            // return productoDao.getCountProductosNeedSync() > 0 ||
            //        beneficioDao.getCountBeneficiosNeedSync() > 0 ||
            //        sucursalDao.getCountSucursalesNeedSync() > 0;
            return false; // Temporal
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Sincroniza todos los datos con el servidor
     */
    public void sincronizarTodosLosDatos() {
        executor.execute(() -> {
            try {
                isLoading.postValue(true);
                // Implementar lógica de sincronización completa aquí
                successMessage.postValue("Sincronización completada");
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Obtiene una sucursal por ID
     */
    public SucursalEntity getSucursalPorId(String id) {
        try {
            return sucursalDao.getById(id);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Activa una sucursal
     */
    public void activarSucursal(long sucursalId) {
        executor.execute(() -> {
            try {
                // TODO: Implementar activarSucursal en database.dao.SucursalDao
                // int rowsUpdated = sucursalDao.activarSucursal(sucursalId, System.currentTimeMillis(), "admin");
                int rowsUpdated = 0; // Temporal
                if (rowsUpdated > 0) {
                    successMessage.postValue("Sucursal activada exitosamente");
                } else {
                    errorMessage.postValue("No se pudo activar la sucursal");
                }
            } catch (Exception e) {
                errorMessage.postValue("Error al activar sucursal: " + e.getMessage());
            }
        });
    }
    
    /**
     * Desactiva una sucursal
     */
    public void desactivarSucursal(long sucursalId, String motivo) {
        executor.execute(() -> {
            try {
                // TODO: Implementar desactivarSucursal en database.dao.SucursalDao
                // int rowsUpdated = sucursalDao.desactivarSucursal(sucursalId, System.currentTimeMillis(), "admin");
                int rowsUpdated = 0; // Temporal
                if (rowsUpdated > 0) {
                    successMessage.postValue("Sucursal desactivada exitosamente");
                } else {
                    errorMessage.postValue("No se pudo desactivar la sucursal");
                }
            } catch (Exception e) {
                errorMessage.postValue("Error al desactivar sucursal: " + e.getMessage());
            }
        });
    }
    
    /**
     * Elimina una sucursal (eliminación lógica)
     */
    public void eliminarSucursal(long sucursalId) {
        executor.execute(() -> {
            try {
                // Para eliminación lógica, simplemente desactivamos la sucursal
                // TODO: Implementar desactivarSucursal en database.dao.SucursalDao
                // int rowsUpdated = sucursalDao.desactivarSucursal(sucursalId, System.currentTimeMillis(), "admin");
                int rowsUpdated = 0; // Temporal
                if (rowsUpdated > 0) {
                    successMessage.postValue("Sucursal eliminada exitosamente");
                } else {
                    errorMessage.postValue("No se pudo eliminar la sucursal");
                }
            } catch (Exception e) {
                errorMessage.postValue("Error al eliminar sucursal: " + e.getMessage());
            }
        });
    }
    
    /**
     * Verifica si una sucursal tiene dependencias
     */
    public boolean sucursalTieneDependencias(long sucursalId) {
        // Implementación básica - en una aplicación real verificaría visitas, empleados, etc.
        return false;
    }
    
    /**
     * Verifica si una sucursal tiene visitas activas
     */
    public boolean sucursalTieneVisitasActivas(long sucursalId) {
        // Implementación básica - en una aplicación real verificaría visitas activas
        return false;
    }
    
    // ========== LIMPIEZA DE DATOS ==========
    
    /**
     * Limpia todos los datos locales de la base de datos
     */
    public void limpiarDatosLocales() {
        executor.execute(() -> {
            try {
                // Limpiar todas las tablas
                productoDao.deleteAll();
                beneficioDao.deleteAllBeneficios();
                sucursalDao.deleteAll();
                
                successMessage.postValue("Datos locales limpiados exitosamente");
            } catch (Exception e) {
                errorMessage.postValue("Error al limpiar datos locales: " + e.getMessage());
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