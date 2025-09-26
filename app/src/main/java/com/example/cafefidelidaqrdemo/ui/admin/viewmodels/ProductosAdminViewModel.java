package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.repository.AdminRepository;
import com.example.cafefidelidaqrdemo.models.Producto;

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
    private final LiveData<List<Producto>> allProductos;
    private final LiveData<List<Producto>> productosActivos;
    private final LiveData<Integer> countProductosActivos;
    private final LiveData<Integer> countProductosInactivos;
    
    // Estados de operaciones
    private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);
    
    // Producto seleccionado para edición
    private final MutableLiveData<Producto> selectedProducto = new MutableLiveData<>();
    
    public ProductosAdminViewModel(@NonNull Application application) {
        super(application);
        
        adminRepository = new AdminRepository(application);
        executor = Executors.newFixedThreadPool(3);
        
        // Inicializar LiveData observables
        allProductos = adminRepository.getAllProductos();
        productosActivos = adminRepository.getProductosActivos();
        countProductosActivos = adminRepository.getCountProductosActivos();
        countProductosInactivos = adminRepository.getCountProductosInactivos();
        
        // Observar los LiveData del repository para sincronizar estados
        adminRepository.getIsLoading().observeForever(value -> isLoading.setValue(value));
        adminRepository.getErrorMessage().observeForever(value -> errorMessage.setValue(value));
        adminRepository.getSuccessMessage().observeForever(value -> successMessage.setValue(value));
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
    
    public LiveData<List<Producto>> getAllProductos() {
        return allProductos;
    }
    
    public LiveData<List<Producto>> getProductosActivos() {
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
    
    public LiveData<Producto> getSelectedProducto() {
        return selectedProducto;
    }
    
    /**
     * Crea un nuevo producto
     */
    public void crearProducto(Producto producto) {
        if (!validarProducto(producto)) {
            return;
        }
        
        isCreating.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si ya existe un producto con el mismo nombre
                if (adminRepository.existeProductoPorNombre(producto.getNombre())) {
                    errorMessage.postValue("Ya existe un producto con ese nombre");
                    isCreating.postValue(false);
                    return;
                }
                
                // Verificar si ya existe un producto con el mismo código
                if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty()) {
                    if (adminRepository.existeProductoPorCodigo(producto.getCodigoBarras())) {
                        errorMessage.postValue("Ya existe un producto con ese código");
                        isCreating.postValue(false);
                        return;
                    }
                }
                
                adminRepository.crearProducto(producto, new AdminRepository.AdminCallback<Producto>() {
                    @Override
                    public void onSuccess(Producto result) {
                        successMessage.postValue("Producto creado exitosamente");
                        isCreating.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al crear producto: " + error);
                        isCreating.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al crear producto: " + e.getMessage());
                isCreating.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza un producto existente
     */
    public void actualizarProducto(Producto producto) {
        if (!validarProducto(producto)) {
            return;
        }
        
        isUpdating.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar que el producto existe
                if (producto.getId() == null || producto.getId().isEmpty()) {
                    errorMessage.postValue("El producto no tiene un ID válido");
                    isUpdating.postValue(false);
                    return;
                }
                
                // Verificar nombres duplicados (excluyendo el producto actual)
                if (adminRepository.existeProductoPorNombreExcluyendoId(producto.getNombre(), producto.getId())) {
                    errorMessage.postValue("Ya existe otro producto con ese nombre");
                    isUpdating.postValue(false);
                    return;
                }
                
                // Actualizar fecha de modificación
                producto.setFechaActualizacion(System.currentTimeMillis());
                
                adminRepository.actualizarProducto(producto, new AdminRepository.AdminCallback<Producto>() {
                    @Override
                    public void onSuccess(Producto result) {
                        successMessage.postValue("Producto actualizado exitosamente");
                        isUpdating.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al actualizar producto: " + error);
                        isUpdating.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar producto: " + e.getMessage());
                isUpdating.postValue(false);
            }
        });
    }
    
    /**
     * Activa un producto
     */
    public void activarProducto(long productoId) {
        adminRepository.activarProducto(productoId, new AdminRepository.AdminCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                successMessage.postValue("Producto activado exitosamente");
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al activar producto: " + error);
            }
        });
    }
    
    /**
     * Desactiva un producto
     */
    public void desactivarProducto(long productoId, String motivo) {
        adminRepository.desactivarProducto(productoId, motivo, new AdminRepository.AdminCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                successMessage.postValue("Producto desactivado exitosamente");
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al desactivar producto: " + error);
            }
        });
    }
    
    /**
     * Elimina un producto (eliminación lógica)
     */
    public void eliminarProducto(long productoId) {
        isDeleting.setValue(true);
        
        // Verificar si el producto está siendo usado
        if (adminRepository.productoTieneDependencias(productoId)) {
            errorMessage.postValue("No se puede eliminar: el producto tiene dependencias. Considere desactivarlo.");
            isDeleting.postValue(false);
            return;
        }
        
        adminRepository.eliminarProducto(productoId, new AdminRepository.AdminCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                successMessage.postValue("Producto eliminado exitosamente");
                isDeleting.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al eliminar producto: " + error);
                isDeleting.postValue(false);
            }
        });
    }
    
    /**
     * Busca productos por nombre o código
     */
    public LiveData<List<Producto>> buscarProductos(String query) {
        return adminRepository.buscarProductos(query);
    }
    
    /**
     * Obtiene productos por categoría
     */
    public LiveData<List<Producto>> getProductosPorCategoria(String categoria) {
        return adminRepository.getProductosPorCategoria(categoria);
    }
    
    /**
     * Obtiene productos con stock bajo
     */
    public LiveData<List<Producto>> getProductosStockBajo(int umbral) {
        return adminRepository.getProductosStockBajo(umbral);
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public void actualizarStock(long productoId, int nuevoStock, String motivo) {
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
            }
        });
    }
    
    /**
     * Actualiza el precio de un producto
     */
    public void actualizarPrecio(long productoId, double nuevoPrecio, String motivo) {
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
            }
        });
    }
    
    /**
     * Actualiza todos los productos desde el servidor
     */
    public void actualizarProductos() {
        executor.execute(() -> {
            try {
                adminRepository.sincronizarProductos();
                successMessage.postValue("Productos actualizados desde el servidor");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar productos: " + e.getMessage());
            }
        });
    }
    
    /**
     * Exporta la lista de productos
     */
    public void exportarProductos() {
        executor.execute(() -> {
            try {
                adminRepository.exportarProductos();
                successMessage.postValue("Productos exportados exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar productos: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sincroniza con el servidor
     */
    public void sincronizarConServidor() {
        executor.execute(() -> {
            try {
                adminRepository.sincronizarProductosConServidor();
                successMessage.postValue("Sincronización completada");
                
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
            }
        });
    }
    
    /**
     * Selecciona un producto para edición
     */
    public void seleccionarProducto(Producto producto) {
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
    private boolean validarProducto(Producto producto) {
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
        
        if (producto.getStock() < 0) {
            errorMessage.setValue("El stock no puede ser negativo");
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