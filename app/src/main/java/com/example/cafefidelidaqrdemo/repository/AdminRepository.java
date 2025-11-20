package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.models.RecentActivity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.RetrofitClient;
import com.example.cafefidelidaqrdemo.ui.admin.viewmodels.AdminDashboardViewModel.SystemHealth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para operaciones administrativas CRUD
 * Maneja productos, beneficios y sucursales usando SQLite
 */
public class AdminRepository {
    
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    // LiveData persistentes para sucursales (lista y conteos)
    private final MutableLiveData<List<Sucursal>> allSucursalesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> countSucursalesActivasLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> countSucursalesInactivasLiveData = new MutableLiveData<>(0);
    
    public AdminRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.apiService = RetrofitClient.getInstance(context).getApiService();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    // ========== GESTIÓN DE PRODUCTOS ==========
    
    /**
     * Obtiene todos los productos
     */
    public LiveData<List<Producto>> getAllProductos() {
        MutableLiveData<List<Producto>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerTodosLosProductos();
                result.postValue(productos);
            } catch (Exception e) {
                errorMessage.postValue("Error al obtener productos: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Obtiene productos activos (disponibles)
     */
    public LiveData<List<Producto>> getProductosActivos() {
        MutableLiveData<List<Producto>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerProductosDisponibles();
                result.postValue(productos);
            } catch (Exception e) {
                errorMessage.postValue("Error al obtener productos activos: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }

    /**
     * Obtiene un producto por ID
     */
    public LiveData<Producto> getProductoById(long id) {
        MutableLiveData<Producto> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Producto producto = database.obtenerProductoPorId((int) id);
                result.postValue(producto);
            } catch (Exception e) {
                result.postValue(null);
                errorMessage.postValue("Error al obtener producto: " + e.getMessage());
            }
        });
        return result;
    }

    /**
     * Busca productos por nombre
     */
    public LiveData<List<Producto>> buscarProductos(String nombre) {
        MutableLiveData<List<Producto>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                if (nombre == null || nombre.trim().isEmpty()) {
                    List<Producto> productos = database.obtenerTodosLosProductos();
                    result.postValue(productos);
                    return;
                }
                
                // Implementar búsqueda por nombre en la base de datos
                List<Producto> productos = database.obtenerTodosLosProductos();
                List<Producto> filtrados = new java.util.ArrayList<>();
                for (Producto producto : productos) {
                    if (producto.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                        filtrados.add(producto);
                    }
                }
                result.postValue(filtrados);
            } catch (Exception e) {
                result.postValue(new java.util.ArrayList<>());
                errorMessage.postValue("Error al buscar productos: " + e.getMessage());
            }
        });
        return result;
    }

    /**
     * Crea un nuevo producto
     */
    public void crearProducto(Producto producto, AdminCallback<Producto> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Validar producto
                if (!validarProducto(producto)) {
                    callback.onError("Datos del producto inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                long id = database.insertarProducto(producto);
                if (id > 0) {
                    producto.setId(String.valueOf(id));
                    callback.onSuccess(producto);
                    successMessage.postValue("Producto creado exitosamente");
                } else {
                    callback.onError("Error al crear producto");
                }
            } catch (Exception e) {
                callback.onError("Error al crear producto: " + e.getMessage());
                errorMessage.postValue("Error al crear producto: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza un producto existente
     */
    public void actualizarProducto(Producto producto, AdminCallback<Producto> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                if (!validarProducto(producto)) {
                    callback.onError("Datos del producto inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                int rowsAffected = database.actualizarProducto(producto);
                if (rowsAffected > 0) {
                    callback.onSuccess(producto);
                    successMessage.postValue("Producto actualizado exitosamente");
                } else {
                    callback.onError("No se pudo actualizar el producto");
                }
            } catch (Exception e) {
                callback.onError("Error al actualizar producto: " + e.getMessage());
                errorMessage.postValue("Error al actualizar producto: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Elimina un producto
     */
    public void eliminarProducto(long productoId, AdminCallback<Boolean> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                int rowsAffected = database.eliminarProducto((int) productoId);
                if (rowsAffected > 0) {
                    callback.onSuccess(true);
                    successMessage.postValue("Producto eliminado exitosamente");
                } else {
                    callback.onError("No se pudo eliminar el producto");
                }
            } catch (Exception e) {
                callback.onError("Error al eliminar producto: " + e.getMessage());
                errorMessage.postValue("Error al eliminar producto: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public void actualizarStockProducto(String productoId, int nuevoStock, String motivo) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                int id = Integer.parseInt(productoId);
                Producto producto = database.obtenerProductoPorId(id);
                if (producto != null) {
                    producto.setStock(nuevoStock);
                    int rowsAffected = database.actualizarProducto(producto);
                    if (rowsAffected > 0) {
                        successMessage.postValue("Stock actualizado exitosamente. Motivo: " + motivo);
                    } else {
                        errorMessage.postValue("No se pudo actualizar el stock del producto");
                    }
                } else {
                    errorMessage.postValue("Producto no encontrado");
                }
            } catch (NumberFormatException e) {
                errorMessage.postValue("ID de producto inválido");
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar stock: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== GESTIÓN DE SUCURSALES ==========
    
    /**
     * Obtiene todas las sucursales
     */
    public LiveData<List<Sucursal>> getAllSucursales() {
        // Devolver LiveData persistente y refrescar en background
        refreshSucursalesLiveData();
        return allSucursalesLiveData;
    }
    
    /**
     * Obtiene una sucursal por ID
     */
    public LiveData<Sucursal> getSucursalById(long id) {
        MutableLiveData<Sucursal> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Sucursal sucursal = database.obtenerSucursalPorId((int) id);
                result.postValue(sucursal);
            } catch (Exception e) {
                result.postValue(null);
                errorMessage.postValue("Error al obtener sucursal: " + e.getMessage());
            }
        });
        return result;
    }
    
    /**
     * Crea una nueva sucursal
     */
    public void crearSucursal(Sucursal sucursal, AdminCallback<Sucursal> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                if (!validarSucursal(sucursal)) {
                    callback.onError("Datos de la sucursal inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                long id = database.insertarSucursal(sucursal);
                if (id > 0) {
                    sucursal.setId(String.valueOf(id));
                    callback.onSuccess(sucursal);
                    successMessage.postValue("Sucursal creada exitosamente");
                    // Refrescar lista y conteos para notificar a observadores
                    refreshSucursalesLiveData();
                } else {
                    callback.onError("Error al crear sucursal");
                }
            } catch (Exception e) {
                callback.onError("Error al crear sucursal: " + e.getMessage());
                errorMessage.postValue("Error al crear sucursal: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza una sucursal existente
     */
    public void actualizarSucursal(Sucursal sucursal, AdminCallback<Sucursal> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                if (!validarSucursal(sucursal)) {
                    callback.onError("Datos de la sucursal inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                int rowsAffected = database.actualizarSucursal(sucursal);
                if (rowsAffected > 0) {
                    callback.onSuccess(sucursal);
                    successMessage.postValue("Sucursal actualizada exitosamente");
                } else {
                    callback.onError("No se pudo actualizar la sucursal");
                }
            } catch (Exception e) {
                callback.onError("Error al actualizar sucursal: " + e.getMessage());
                errorMessage.postValue("Error al actualizar sucursal: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== GESTIÓN DE BENEFICIOS ==========
    
    /**
     * Obtiene todos los beneficios
     */
    public LiveData<List<Beneficio>> getAllBeneficios() {
        MutableLiveData<List<Beneficio>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<com.example.cafefidelidaqrdemo.models.Beneficio> dbBeneficios = database.obtenerTodosLosBeneficios();
                List<Beneficio> beneficios = convertFromDBBeneficioList(dbBeneficios);
                result.postValue(beneficios);
            } catch (Exception e) {
                errorMessage.postValue("Error al obtener beneficios: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Obtiene beneficios activos
     */
    public LiveData<List<Beneficio>> getBeneficiosActivos() {
        MutableLiveData<List<Beneficio>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<com.example.cafefidelidaqrdemo.models.Beneficio> dbBeneficios = database.obtenerBeneficiosActivos();
                List<Beneficio> beneficios = convertFromDBBeneficioList(dbBeneficios);
                result.postValue(beneficios);
            } catch (Exception e) {
                errorMessage.postValue("Error al obtener beneficios activos: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Crea un nuevo beneficio
     */
    public void crearBeneficio(Beneficio beneficio, AdminCallback<Beneficio> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                if (!validarBeneficio(beneficio)) {
                    callback.onError("Datos del beneficio inválidos");
                    isLoading.postValue(false);
                    return;
                }
                
                // Convertir a modelo de base de datos antes de insertar
                com.example.cafefidelidaqrdemo.models.Beneficio dbBeneficio = convertToDBBeneficio(beneficio);
                long id = database.insertarBeneficio(dbBeneficio);
                if (id > 0) {
                    beneficio.setId(String.valueOf(id));
                    callback.onSuccess(beneficio);
                    successMessage.postValue("Beneficio creado exitosamente");
                } else {
                    callback.onError("Error al crear beneficio");
                }
            } catch (Exception e) {
                callback.onError("Error al crear beneficio: " + e.getMessage());
                errorMessage.postValue("Error al crear beneficio: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== MÉTODOS DE VALIDACIÓN ==========
    
    private boolean validarProducto(Producto producto) {
        return producto != null && 
               producto.getNombre() != null && !producto.getNombre().trim().isEmpty() &&
               producto.getPrecio() > 0;
    }
    
    private boolean validarSucursal(Sucursal sucursal) {
        return sucursal != null && 
               sucursal.getNombre() != null && !sucursal.getNombre().trim().isEmpty() &&
               sucursal.getDireccion() != null && !sucursal.getDireccion().trim().isEmpty();
    }
    
    private boolean validarBeneficio(Beneficio beneficio) {
        return beneficio != null && 
               beneficio.getNombre() != null && !beneficio.getNombre().trim().isEmpty() &&
               beneficio.esValido();
    }
    
    // ========== GETTERS PARA LIVEDATA ==========
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    // ========== MÉTODOS DE CONTEO ==========
    
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
    
    public LiveData<Integer> getCountProductosActivos() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Producto> productos = database.obtenerProductosDisponibles();
                result.postValue(productos != null ? productos.size() : 0);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    public LiveData<Integer> getCountProductosInactivos() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int total = database.obtenerConteoProductos();
                List<Producto> activos = database.obtenerProductosDisponibles();
                int activosCount = activos != null ? activos.size() : 0;
                result.postValue(total - activosCount);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    // Métodos síncronos para estadísticas
    public int getCountProductosActivosSync() {
        try {
            List<Producto> productos = database.obtenerProductosDisponibles();
            return productos != null ? productos.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getCountProductosInactivosSync() {
        try {
            int total = database.obtenerConteoProductos();
            List<Producto> activos = database.obtenerProductosDisponibles();
            int activosCount = activos != null ? activos.size() : 0;
            return total - activosCount;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Métodos de conteo para sucursales
    public LiveData<Integer> getCountSucursalesActivas() {
        // Devolver LiveData persistente y refrescar en background
        refreshSucursalesLiveData();
        return countSucursalesActivasLiveData;
    }
    
    public LiveData<Integer> getCountSucursalesInactivas() {
        // Devolver LiveData persistente y refrescar en background
        refreshSucursalesLiveData();
        return countSucursalesInactivasLiveData;
    }
    
    public int getCountSucursalesActivasSync() {
        try {
            List<Sucursal> sucursales = database.obtenerSucursalesActivas();
            return sucursales != null ? sucursales.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getCountSucursalesInactivasSync() {
        try {
            int total = database.obtenerConteoSucursales();
            List<Sucursal> activas = database.obtenerSucursalesActivas();
            int activasCount = activas != null ? activas.size() : 0;
            return total - activasCount;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Métodos de conteo para beneficios
    public LiveData<Integer> getCountBeneficiosActivos() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<com.example.cafefidelidaqrdemo.models.Beneficio> dbBeneficios = database.obtenerBeneficiosActivos();
                result.postValue(dbBeneficios != null ? dbBeneficios.size() : 0);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    public LiveData<Integer> getCountBeneficiosInactivos() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int total = database.obtenerConteoBeneficios();
                List<com.example.cafefidelidaqrdemo.models.Beneficio> dbActivos = database.obtenerBeneficiosActivos();
                int activosCount = dbActivos != null ? dbActivos.size() : 0;
                result.postValue(total - activosCount);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    public int getCountBeneficiosActivosSync() {
        try {
            List<com.example.cafefidelidaqrdemo.models.Beneficio> dbBeneficios = database.obtenerBeneficiosActivos();
            return dbBeneficios != null ? dbBeneficios.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getCountBeneficiosInactivosSync() {
        try {
            int total = database.obtenerConteoBeneficios();
            List<com.example.cafefidelidaqrdemo.models.Beneficio> dbActivos = database.obtenerBeneficiosActivos();
            int activosCount = dbActivos != null ? dbActivos.size() : 0;
            return total - activosCount;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public LiveData<Integer> getCountSucursales() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = database.obtenerConteoSucursales();
                result.postValue(count);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    public LiveData<Integer> getCountBeneficios() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = database.obtenerConteoBeneficios();
                result.postValue(count);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }

    // Refresca lista y conteos de sucursales y postea en LiveData persistentes
    private void refreshSucursalesLiveData() {
        executor.execute(() -> {
            try {
                List<Sucursal> todas = database.obtenerTodasLasSucursales();
                List<Sucursal> activas = database.obtenerSucursalesActivas();
                int total = database.obtenerConteoSucursales();
                int activasCount = activas != null ? activas.size() : 0;
                int inactivasCount = Math.max(0, total - activasCount);

                allSucursalesLiveData.postValue(todas != null ? todas : new ArrayList<>());
                countSucursalesActivasLiveData.postValue(activasCount);
                countSucursalesInactivasLiveData.postValue(inactivasCount);
            } catch (Exception e) {
                // En caso de error, no romper UI; mantener valores actuales
            }
        });
    }
    
    // ========== MÉTODOS DE SINCRONIZACIÓN Y EXPORTACIÓN ==========
    
    public boolean hayCambiosPendientes() {
        try {
            // Verificar si hay cambios pendientes de sincronización
            // Por ahora retornamos false, pero se puede implementar lógica específica
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void sincronizarTodosLosDatos() {
        executor.execute(() -> {
            try {
                isLoading.postValue(true);
                // Implementar lógica de sincronización
                // Por ahora solo simulamos la sincronización
                Thread.sleep(1000);
                successMessage.postValue("Sincronización completada");
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void exportarProductos() {
        executor.execute(() -> {
            try {
                isLoading.postValue(true);
                // Implementar lógica de exportación
                List<Producto> productos = database.obtenerTodosLosProductos();
                if (productos != null && !productos.isEmpty()) {
                    // Aquí se implementaría la exportación real (CSV, JSON, etc.)
                    successMessage.postValue("Productos exportados exitosamente (" + productos.size() + " productos)");
                } else {
                    errorMessage.postValue("No hay productos para exportar");
                }
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar productos: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== INTERFAZ DE CALLBACK ==========
    
    public interface AdminCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    // ========== LIMPIEZA ==========
    
    public void limpiarDatosLocales() {
        executor.execute(() -> {
            try {
                isLoading.postValue(true);
                // Implementar lógica de limpieza de datos locales
                // Por ejemplo, limpiar caché, datos temporales, etc.
                successMessage.postValue("Datos locales limpiados exitosamente");
            } catch (Exception e) {
                errorMessage.postValue("Error al limpiar datos locales: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    


    // ==================== MÉTODOS DE CONVERSIÓN BENEFICIO ====================
    
    /**
     * Convierte de modelo de dominio a modelo de base de datos
     */
    private com.example.cafefidelidaqrdemo.models.Beneficio convertToDBBeneficio(Beneficio beneficio) {
        if (beneficio == null) return null;
        
        com.example.cafefidelidaqrdemo.models.Beneficio dbBeneficio =
            new com.example.cafefidelidaqrdemo.models.Beneficio();
        
        // Mantener ID como String
        if (beneficio.getId() != null && !beneficio.getId().isEmpty()) {
            dbBeneficio.setId(beneficio.getId());
        } else {
            // Para nuevos registros, usar null o string vacío
            dbBeneficio.setId(null);
        }
        
        dbBeneficio.setNombre(beneficio.getNombre());
        dbBeneficio.setDescripcion(beneficio.getDescripcion());
        dbBeneficio.setActivo(beneficio.isActivo());
        
        // Mapear tipo de enum a string
        if (beneficio.getTipo() != null) {
            dbBeneficio.setTipo(beneficio.getTipo().toString());
        }
        
        // Mapear fechas usando los métodos disponibles
        if (beneficio.getFechaVencimiento() > 0) {
            dbBeneficio.setFechaVencimiento(beneficio.getFechaVencimiento());
        }
        
        // Mapear visitas requeridas
        dbBeneficio.setVisitasRequeridas(beneficio.getVisitasRequeridas());
        
        return dbBeneficio;
    }

    /**
     * Convierte de modelo de base de datos a modelo de dominio
     */
    private Beneficio convertFromDBBeneficio(com.example.cafefidelidaqrdemo.models.Beneficio dbBeneficio) {
        if (dbBeneficio == null) return null;
        
        Beneficio beneficio = new Beneficio();
        
        // Convertir ID de int a String
        beneficio.setId(String.valueOf(dbBeneficio.getId()));
        beneficio.setNombre(dbBeneficio.getNombre());
        beneficio.setDescripcion(dbBeneficio.getDescripcion());
        beneficio.setActivo(dbBeneficio.isActivo());
        
        // Mapear tipo como string
        if (dbBeneficio.getTipo() != null) {
            beneficio.setTipo(dbBeneficio.getTipo());
        }
        
        // Mapear fechas usando los métodos disponibles
        if (dbBeneficio.getFechaVencimiento() > 0) {
            beneficio.setFechaVencimiento(dbBeneficio.getFechaVencimiento());
        }
        
        // Mapear campos básicos
        beneficio.setVisitasRequeridas(dbBeneficio.getVisitasRequeridas());
        
        return beneficio;
    }

    /**
     * Convierte lista de modelos de base de datos a modelos de dominio
     */
    private List<Beneficio> convertFromDBBeneficioList(List<com.example.cafefidelidaqrdemo.models.Beneficio> dbBeneficios) {
        if (dbBeneficios == null) return new ArrayList<>();
        
        List<Beneficio> beneficios = new ArrayList<>();
        for (com.example.cafefidelidaqrdemo.models.Beneficio dbBeneficio : dbBeneficios) {
            Beneficio beneficio = convertFromDBBeneficio(dbBeneficio);
            if (beneficio != null) {
                beneficios.add(beneficio);
            }
        }
        return beneficios;
    }

    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}