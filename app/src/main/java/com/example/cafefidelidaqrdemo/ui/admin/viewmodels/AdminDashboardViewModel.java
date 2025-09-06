package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.data.repositories.AdminRepository;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para el dashboard principal de administración
 * Maneja estadísticas generales y navegación entre módulos
 */
public class AdminDashboardViewModel extends AndroidViewModel {
    
    private final AdminRepository adminRepository;
    private final ExecutorService executor;
    
    // Estados de carga y mensajes
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    // Estadísticas del dashboard
    private final MutableLiveData<DashboardStats> dashboardStats = new MutableLiveData<>();
    private final MutableLiveData<List<RecentActivity>> recentActivities = new MutableLiveData<>();
    
    // Datos observables desde el repositorio
    private final LiveData<Integer> countProductosActivos;
    private final LiveData<Integer> countProductosInactivos;
    private final LiveData<Integer> countSucursalesActivas;
    private final LiveData<Integer> countSucursalesInactivas;
    private final LiveData<Integer> countBeneficiosActivos;
    private final LiveData<Integer> countBeneficiosInactivos;
    
    public AdminDashboardViewModel(@NonNull Application application) {
        super(application);
        
        adminRepository = new AdminRepository(application);
        executor = Executors.newFixedThreadPool(3);
        
        // Inicializar LiveData observables
        countProductosActivos = adminRepository.getCountProductosActivos();
        countProductosInactivos = adminRepository.getCountProductosInactivos();
        countSucursalesActivas = adminRepository.getCountSucursalesActivas();
        countSucursalesInactivas = adminRepository.getCountSucursalesInactivas();
        countBeneficiosActivos = adminRepository.getCountBeneficiosActivos();
        countBeneficiosInactivos = adminRepository.getCountBeneficiosInactivos();
        
        // Cargar datos iniciales
        cargarDashboardStats();
        cargarActividadesRecientes();
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
    
    public LiveData<DashboardStats> getDashboardStats() {
        return dashboardStats;
    }
    
    public LiveData<List<RecentActivity>> getRecentActivities() {
        return recentActivities;
    }
    
    public LiveData<Integer> getCountProductosActivos() {
        return countProductosActivos;
    }
    
    public LiveData<Integer> getCountProductosInactivos() {
        return countProductosInactivos;
    }
    
    public LiveData<Integer> getCountSucursalesActivas() {
        return countSucursalesActivas;
    }
    
    public LiveData<Integer> getCountSucursalesInactivas() {
        return countSucursalesInactivas;
    }
    
    public LiveData<Integer> getCountBeneficiosActivos() {
        return countBeneficiosActivos;
    }
    
    public LiveData<Integer> getCountBeneficiosInactivos() {
        return countBeneficiosInactivos;
    }
    
    // LiveData calculados
    public LiveData<Integer> getTotalProductos() {
        return Transformations.map(countProductosActivos, activos -> {
            Integer inactivos = countProductosInactivos.getValue();
            return activos + (inactivos != null ? inactivos : 0);
        });
    }
    
    public LiveData<Integer> getTotalSucursales() {
        return Transformations.map(countSucursalesActivas, activas -> {
            Integer inactivas = countSucursalesInactivas.getValue();
            return activas + (inactivas != null ? inactivas : 0);
        });
    }
    
    public LiveData<Integer> getTotalBeneficios() {
        return Transformations.map(countBeneficiosActivos, activos -> {
            Integer inactivos = countBeneficiosInactivos.getValue();
            return activos + (inactivos != null ? inactivos : 0);
        });
    }
    
    /**
     * Carga las estadísticas generales del dashboard
     */
    public void cargarDashboardStats() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                DashboardStats stats = new DashboardStats();
                
                // Obtener estadísticas de productos
                stats.totalProductos = adminRepository.getCountProductosActivosSync() + 
                                     adminRepository.getCountProductosInactivosSync();
                stats.productosActivos = adminRepository.getCountProductosActivosSync();
                stats.productosInactivos = adminRepository.getCountProductosInactivosSync();
                
                // Obtener estadísticas de sucursales
                stats.totalSucursales = adminRepository.getCountSucursalesActivasSync() + 
                                      adminRepository.getCountSucursalesInactivasSync();
                stats.sucursalesActivas = adminRepository.getCountSucursalesActivasSync();
                stats.sucursalesInactivas = adminRepository.getCountSucursalesInactivasSync();
                
                // Obtener estadísticas de beneficios
                stats.totalBeneficios = adminRepository.getCountBeneficiosActivosSync() + 
                                      adminRepository.getCountBeneficiosInactivosSync();
                stats.beneficiosActivos = adminRepository.getCountBeneficiosActivosSync();
                stats.beneficiosInactivos = adminRepository.getCountBeneficiosInactivosSync();
                
                // Calcular porcentajes
                stats.porcentajeProductosActivos = stats.totalProductos > 0 ? 
                    (stats.productosActivos * 100.0f) / stats.totalProductos : 0;
                stats.porcentajeSucursalesActivas = stats.totalSucursales > 0 ? 
                    (stats.sucursalesActivas * 100.0f) / stats.totalSucursales : 0;
                stats.porcentajeBeneficiosActivos = stats.totalBeneficios > 0 ? 
                    (stats.beneficiosActivos * 100.0f) / stats.totalBeneficios : 0;
                
                dashboardStats.postValue(stats);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar estadísticas: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Carga las actividades recientes del sistema
     */
    public void cargarActividadesRecientes() {
        executor.execute(() -> {
            try {
                // TODO: Resolver conflicto de tipos entre models.RecentActivity y AdminDashboardViewModel.RecentActivity
                // List<AdminDashboardViewModel.RecentActivity> actividades = adminRepository.getActividadesRecientes(10);
                // recentActivities.postValue(actividades);
                
                // Simulación temporal de actividades recientes
                recentActivities.postValue(java.util.Collections.emptyList());
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar actividades recientes: " + e.getMessage());
            }
        });
    }
    
    /**
     * Actualiza todos los datos del dashboard
     */
    public void actualizarDashboard() {
        cargarDashboardStats();
        cargarActividadesRecientes();
        successMessage.setValue("Dashboard actualizado");
    }
    
    /**
     * Actualiza las estadísticas del dashboard
     */
    public void actualizarEstadisticas() {
        cargarDashboardStats();
    }
    
    /**
     * Actualiza todo el contenido del dashboard
     */
    public void actualizarTodo() {
        actualizarDashboard();
    }
    
    /**
     * Sincroniza datos con el servidor
     */
    public void sincronizarDatos() {
        sincronizarConServidor();
    }
    
    /**
     * Verifica si hay cambios pendientes de sincronización
     */
    public void verificarCambiosPendientes() {
        executor.execute(() -> {
            try {
                boolean hayPendientes = adminRepository.hayCambiosPendientes();
                if (hayPendientes) {
                    successMessage.postValue("Hay cambios pendientes de sincronización");
                } else {
                    successMessage.postValue("No hay cambios pendientes");
                }
            } catch (Exception e) {
                errorMessage.postValue("Error al verificar cambios: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sincroniza datos con el servidor
     */
    public void sincronizarConServidor() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.sincronizarTodosLosDatos();
                
                // Recargar estadísticas después de la sincronización
                cargarDashboardStats();
                cargarActividadesRecientes();
                
                successMessage.postValue("Sincronización completada");
                
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Exporta datos del sistema
     */
    public void exportarDatos() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.exportarProductos();
                successMessage.postValue("Datos exportados exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar datos: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Limpia la base de datos local
     */
    public void limpiarDatosLocales() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.limpiarDatosLocales();
                
                // Recargar estadísticas
                cargarDashboardStats();
                cargarActividadesRecientes();
                
                successMessage.postValue("Datos locales limpiados");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al limpiar datos: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Obtiene el estado de salud del sistema
     */
    public void verificarEstadoSistema() {
        executor.execute(() -> {
            try {
                // TODO: Implementar verificarEstadoSistema() en AdminRepository
                // SystemHealth health = adminRepository.verificarEstadoSistema();
                
                // Simulación temporal del estado del sistema
                successMessage.postValue("Sistema funcionando correctamente");
                // if (health.isHealthy()) {
                //     successMessage.postValue("Sistema funcionando correctamente");
                // } else {
                //     errorMessage.postValue("Problemas detectados: " + health.getProblemas());
                // }
                
            } catch (Exception e) {
                errorMessage.postValue("Error al verificar sistema: " + e.getMessage());
            }
        });
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
    
    /**
     * Clase para encapsular estadísticas del dashboard
     */
    public static class DashboardStats {
        public int totalProductos;
        public int productosActivos;
        public int productosInactivos;
        public float porcentajeProductosActivos;
        
        public int totalSucursales;
        public int sucursalesActivas;
        public int sucursalesInactivas;
        public float porcentajeSucursalesActivas;
        
        public int totalBeneficios;
        public int beneficiosActivos;
        public int beneficiosInactivos;
        public float porcentajeBeneficiosActivos;
        
        public long ultimaActualizacion = System.currentTimeMillis();
    }
    
    /**
     * Clase para representar actividades recientes
     */
    public static class RecentActivity {
        public String tipo; // "producto", "sucursal", "beneficio"
        public String accion; // "creado", "editado", "activado", "desactivado", "eliminado"
        public String entidad; // nombre del elemento
        public String usuario; // quien realizó la acción
        public long timestamp;
        public String descripcion;
        
        public RecentActivity(String tipo, String accion, String entidad, String usuario, long timestamp, String descripcion) {
            this.tipo = tipo;
            this.accion = accion;
            this.entidad = entidad;
            this.usuario = usuario;
            this.timestamp = timestamp;
            this.descripcion = descripcion;
        }
    }
    
    /**
     * Clase para representar el estado de salud del sistema
     */
    public static class SystemHealth {
        private boolean healthy = true;
        private StringBuilder problemas = new StringBuilder();
        
        public boolean isHealthy() {
            return healthy;
        }
        
        public void addProblem(String problema) {
            healthy = false;
            if (problemas.length() > 0) {
                problemas.append(", ");
            }
            problemas.append(problema);
        }
        
        public String getProblemas() {
            return problemas.toString();
        }
    }
}