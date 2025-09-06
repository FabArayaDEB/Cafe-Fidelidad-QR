package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.data.repositories.AdminRepository;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para la gestión CRUD de productos
 * Maneja la lógica de negocio y validaciones para productos
 */
public class ProductosAdminViewModel extends AndroidViewModel {
    
    private final AdminRepository adminRepository;
    private final ExecutorService executor;
    
    // Estados de carga y mensajes
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    // Datos de productos
    private final LiveData<List<ProductoEntity>> allProductos;
    private final LiveData<List<ProductoEntity>> productosActivos;
    private final LiveData<Integer> countProductosActivos;
    private final LiveData<Integer> countProductosInactivos;
    
    // Estados de operaciones
    private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);
    
    // Producto seleccionado para edición
    private final MutableLiveData<ProductoEntity> selectedProducto = new MutableLiveData<>();
    
    public ProductosAdminViewModel(@NonNull Application application) {
        super(application);
        
        adminRepository = new AdminRepository(application);
        executor = Executors.newFixedThreadPool(3);
        
        // Inicializar LiveData observables
        allProductos = adminRepository.getAllProductos();
        productosActivos = adminRepository.getProductosActivos();
        countProductosActivos = adminRepository.getCountProductosActivos();
        countProductosInactivos = adminRepository.getCountProductosInactivos();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    public LiveData<List<ProductoEntity>> getAllProductos() {
        return allProductos;
    }
    
    public LiveData<List<ProductoEntity>> getProductosActivos() {
        return productosActivos;
    }
    
    public LiveData<Integer> getCountProductosActivos() {
        return countProductosActivos;
    }
    
    public LiveData<Integer> getCountProductosInactivos() {
        return countProductosInactivos;
    }
    
    public LiveData<Boolean> getIsCreating() {
        return isCreating;
    }
    
    public LiveData<Boolean> getIsUpdating() {
        return isUpdating;
    }
    
    public LiveData<Boolean> getIsDeleting() {
        return isDeleting;
    }
    
    public LiveData<ProductoEntity> getSelectedProducto() {
        return selectedProducto;
    }
    
    /**
     * Crea un nuevo producto
     */
    public void crearProducto(ProductoEntity producto) {
        if (!validarProducto(producto)) {
            return;
        }
        
        isCreating.setValue(true);
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si ya existe un producto con el mismo nombre
                if (adminRepository.existeProductoPorNombre(producto.getNombre())) {
                    errorMessage.postValue("Ya existe un producto con ese nombre");
                    return;
                }
                
                // Verificar si ya existe un producto con el mismo código
                if (producto.getCodigo() != null && !producto.getCodigo().isEmpty()) {
                    if (adminRepository.existeProductoPorCodigo(producto.getCodigo())) {
                        errorMessage.postValue("Ya existe un producto con ese código");
                        return;
                    }
                }
                
                adminRepository.crearProducto(producto, new AdminRepository.AdminCallback<ProductoEntity>() {
                    @Override
                    public void onSuccess(ProductoEntity result) {
                        successMessage.postValue("Producto creado exitosamente");
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al crear producto: " + error);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al crear producto: " + e.getMessage());
            } finally {
                isCreating.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza un producto existente
     */
    public void actualizarProducto(ProductoEntity producto) {
        if (!validarProducto(producto)) {
            return;
        }
        
        isUpdating.setValue(true);
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar control de versión
                ProductoEntity productoActual = adminRepository.getProductoPorId(producto.getId_producto());
                if (productoActual == null) {
                    errorMessage.postValue("El producto no existe");
                    return;
                }
                
                if (productoActual.getVersion() != producto.getVersion()) {
                    errorMessage.postValue("El producto ha sido modificado por otro usuario. Actualice y vuelva a intentar.");
                    return;
                }
                
                // Verificar nombres duplicados (excluyendo el producto actual)
                if (adminRepository.existeProductoPorNombreExcluyendoId(producto.getNombre(), producto.getId_producto())) {
                    errorMessage.postValue("Ya existe otro producto con ese nombre");
                    return;
                }
                
                // Verificar códigos duplicados (excluyendo el producto actual)
                if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty() &&
                    adminRepository.existeProductoPorCodigoExcluyendoId(producto.getCodigoBarras(), producto.getId_producto())) {
                    errorMessage.postValue("Ya existe otro producto con ese código");
                    return;
                }
                
                adminRepository.actualizarProducto(producto, new AdminRepository.AdminCallback<ProductoEntity>() {
                    @Override
                    public void onSuccess(ProductoEntity result) {
                        successMessage.postValue("Producto actualizado exitosamente");
                        isUpdating.postValue(false);
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al actualizar producto: " + error);
                        isUpdating.postValue(false);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar producto: " + e.getMessage());
                isUpdating.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Activa un producto
     */
    public void activarProducto(long productoId) {
        isLoading.setValue(true);
        
        adminRepository.activarProducto(productoId, new AdminRepository.AdminCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                successMessage.postValue("Producto activado exitosamente");
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al activar producto: " + error);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Desactiva un producto
     */
    public void desactivarProducto(long productoId, String motivo) {
        isLoading.setValue(true);
        
        adminRepository.desactivarProducto(productoId, motivo, new AdminRepository.AdminCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                successMessage.postValue("Producto desactivado exitosamente");
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al desactivar producto: " + error);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Elimina un producto (eliminación lógica)
     */
    public void eliminarProducto(long productoId) {
        isDeleting.setValue(true);
        isLoading.setValue(true);
        
        // Verificar si el producto está siendo usado
        if (adminRepository.productoTieneDependencias(productoId)) {
            errorMessage.postValue("No se puede eliminar: el producto tiene dependencias. Considere desactivarlo.");
            isDeleting.postValue(false);
            isLoading.postValue(false);
            return;
        }
        
        adminRepository.eliminarProducto(productoId, new AdminRepository.AdminCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                successMessage.postValue("Producto eliminado exitosamente");
                isDeleting.postValue(false);
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al eliminar producto: " + error);
                isDeleting.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Busca productos por nombre o código
     */
    public LiveData<List<ProductoEntity>> buscarProductos(String query) {
        return adminRepository.buscarProductos(query);
    }
    
    /**
     * Obtiene productos por categoría
     */
    public LiveData<List<ProductoEntity>> getProductosPorCategoria(String categoria) {
        return adminRepository.getProductosPorCategoria(categoria);
    }
    
    /**
     * Obtiene productos con stock bajo
     */
    public LiveData<List<ProductoEntity>> getProductosStockBajo(int umbral) {
        return adminRepository.getProductosStockBajo(umbral);
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public void actualizarStock(long productoId, int nuevoStock, String motivo) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                if (nuevoStock < 0) {
                    errorMessage.postValue("El stock no puede ser negativo");
                    return;
                }
                
                adminRepository.actualizarStockProducto(productoId, nuevoStock, motivo);
                successMessage.postValue("Stock actualizado exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar stock: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza el precio de un producto
     */
    public void actualizarPrecio(long productoId, double nuevoPrecio, String motivo) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                if (nuevoPrecio < 0) {
                    errorMessage.postValue("El precio no puede ser negativo");
                    return;
                }
                
                adminRepository.actualizarPrecioProducto(productoId, nuevoPrecio, motivo);
                successMessage.postValue("Precio actualizado exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar precio: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza todos los productos desde el servidor
     */
    public void actualizarProductos() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.sincronizarProductos();
                successMessage.postValue("Productos actualizados desde el servidor");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar productos: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Exporta la lista de productos
     */
    public void exportarProductos() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.exportarProductos();
                successMessage.postValue("Productos exportados exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar productos: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Sincroniza con el servidor
     */
    public void sincronizarConServidor() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.sincronizarProductosConServidor();
                successMessage.postValue("Sincronización completada");
                
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Selecciona un producto para edición
     */
    public void seleccionarProducto(ProductoEntity producto) {
        selectedProducto.setValue(producto);
    }
    
    /**
     * Limpia la selección de producto
     */
    public void limpiarSeleccion() {
        selectedProducto.setValue(null);
    }
    
    /**
     * Valida los datos de un producto
     */
    private boolean validarProducto(ProductoEntity producto) {
        if (producto == null) {
            errorMessage.setValue("Datos de producto inválidos");
            return false;
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            errorMessage.setValue("El nombre del producto es obligatorio");
            return false;
        }
        
        if (producto.getNombre().length() > 100) {
            errorMessage.setValue("El nombre del producto no puede exceder 100 caracteres");
            return false;
        }
        
        if (producto.getPrecio() < 0) {
            errorMessage.setValue("El precio no puede ser negativo");
            return false;
        }
        
        if (producto.getStockDisponible() < 0) {
            errorMessage.setValue("El stock no puede ser negativo");
            return false;
        }
        
        if (producto.getCodigoBarras() != null && producto.getCodigoBarras().length() > 50) {
            errorMessage.setValue("El código del producto no puede exceder 50 caracteres");
            return false;
        }
        
        if (producto.getDescripcion() != null && producto.getDescripcion().length() > 500) {
            errorMessage.setValue("La descripción no puede exceder 500 caracteres");
            return false;
        }
        
        return true;
    }
    
    /**
     * Limpia mensajes de error y éxito
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
    
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}