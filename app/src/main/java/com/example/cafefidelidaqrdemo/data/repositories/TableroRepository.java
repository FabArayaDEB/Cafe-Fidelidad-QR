package com.example.cafefidelidaqrdemo.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.data.dao.TableroDao;
import com.example.cafefidelidaqrdemo.data.entities.TableroEntity;
import com.example.cafefidelidaqrdemo.network.ApiClient;
import com.example.cafefidelidaqrdemo.network.ApiService;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository para gestionar KPIs del tablero personal del cliente
 * Maneja cache, sincronización y datos offline
 */
public class TableroRepository {
    
    private final TableroDao tableroDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para estados observables
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isOfflineMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> dataSource = new MutableLiveData<>("cache"); // "cache", "api", "mixed"
    
    public TableroRepository(Context context, TableroDao tableroDao) {
        this.context = context;
        this.tableroDao = tableroDao;
        this.apiService = ApiClient.getApiService();
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // Constructor alternativo para uso desde ViewModels
    public TableroRepository(Context context) {
        this.context = context;
        // Obtener TableroDao desde la base de datos
        // this.tableroDao = CafeFidelidadDatabase.getInstance(context).tableroDao();
        this.tableroDao = null; // Temporal hasta que se implemente la base de datos
        this.apiService = ApiClient.getApiService();
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // ==================== GETTERS PARA LIVEDATA ====================
    
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getIsSyncing() { return isSyncing; }
    public LiveData<Boolean> getIsOfflineMode() { return isOfflineMode; }
    public LiveData<String> getDataSource() { return dataSource; }
    
    // ==================== OPERACIONES PRINCIPALES ====================
    
    public LiveData<TableroEntity> obtenerTableroCliente(String clienteId) {
        return tableroDao.obtenerPorClienteLiveData(clienteId);
    }
    
    public void cargarTableroCliente(String clienteId, RepositoryCallback<TableroEntity> callback) {
        isLoading.postValue(true);
        
        executor.execute(() -> {
            try {
                // Primero intentar obtener desde cache
                TableroEntity tableroCache = tableroDao.obtenerPorCliente(clienteId);
                
                if (tableroCache != null && !tableroCache.esCacheExpirado()) {
                    // Cache válido, usar datos locales
                    dataSource.postValue("cache");
                    if (callback != null) callback.onSuccess(tableroCache);
                    
                    // Refrescar en segundo plano si es posible
                    refrescarTableroEnSegundoPlano(clienteId);
                } else {
                    // Cache expirado o no existe, intentar obtener desde API
                    obtenerTableroDesdeApi(clienteId, new RepositoryCallback<TableroEntity>() {
                        @Override
                        public void onSuccess(TableroEntity tableroApi) {
                            executor.execute(() -> {
                                try {
                                    // Guardar en cache
                                    tableroApi.actualizarCache();
                                    tableroApi.marcarComoSincronizado();
                                    tableroDao.insertar(tableroApi);
                                    
                                    dataSource.postValue("api");
                                    isOfflineMode.postValue(false);
                                    if (callback != null) callback.onSuccess(tableroApi);
                                } catch (Exception e) {
                                    errorMessage.postValue("Error al guardar datos: " + e.getMessage());
                                    if (callback != null) callback.onError(e.getMessage());
                                }
                            });
                        }
                        
                        @Override
                        public void onError(String error) {
                            // Error de API, usar cache aunque esté expirado
                            if (tableroCache != null) {
                                dataSource.postValue("cache");
                                isOfflineMode.postValue(true);
                                successMessage.postValue("Mostrando datos locales (sin conexión)");
                                if (callback != null) callback.onSuccess(tableroCache);
                            } else {
                                errorMessage.postValue("Sin datos disponibles y sin conexión");
                                if (callback != null) callback.onError("Sin datos disponibles");
                            }
                        }
                    });
                }
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar tablero: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void refrescarTablero(String clienteId, RepositoryCallback<TableroEntity> callback) {
        isSyncing.postValue(true);
        
        obtenerTableroDesdeApi(clienteId, new RepositoryCallback<TableroEntity>() {
            @Override
            public void onSuccess(TableroEntity tableroApi) {
                executor.execute(() -> {
                    try {
                        tableroApi.actualizarCache();
                        tableroApi.marcarComoSincronizado();
                        tableroDao.insertar(tableroApi);
                        
                        dataSource.postValue("api");
                        isOfflineMode.postValue(false);
                        successMessage.postValue("Datos actualizados");
                        if (callback != null) callback.onSuccess(tableroApi);
                    } catch (Exception e) {
                        errorMessage.postValue("Error al actualizar datos: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    } finally {
                        isSyncing.postValue(false);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Error al refrescar: " + error);
                isOfflineMode.postValue(true);
                if (callback != null) callback.onError(error);
                isSyncing.postValue(false);
            }
        });
    }
    
    public void actualizarTableroLocal(TableroEntity tablero, RepositoryCallback<TableroEntity> callback) {
        executor.execute(() -> {
            try {
                tablero.incrementarVersion();
                tableroDao.actualizar(tablero);
                
                successMessage.postValue("Tablero actualizado localmente");
                if (callback != null) callback.onSuccess(tablero);
                
                // Intentar sincronizar en segundo plano
                sincronizarTableroEnSegundoPlano(tablero);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar tablero: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    // ==================== MÉTRICAS Y ESTADÍSTICAS ====================
    
    public void obtenerMetricasGlobales(RepositoryCallback<MetricasGlobales> callback) {
        executor.execute(() -> {
            try {
                MetricasGlobales metricas = new MetricasGlobales();
                
                metricas.totalVisitasGlobal = tableroDao.obtenerTotalVisitasGlobal();
                metricas.totalPuntosGlobal = tableroDao.obtenerTotalPuntosGlobal();
                metricas.totalCanjesGlobal = tableroDao.obtenerTotalCanjesGlobal();
                metricas.promedioVisitas = tableroDao.obtenerPromedioVisitas();
                metricas.promedioPuntos = tableroDao.obtenerPromedioPuntosDisponibles();
                metricas.promedioCanjes = tableroDao.obtenerPromedioCanjes();
                metricas.clientesActivos = tableroDao.obtenerClientesActivos().size();
                metricas.clientesInactivos = tableroDao.obtenerClientesInactivos().size();
                
                if (callback != null) callback.onSuccess(metricas);
                
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void obtenerTopClientes(int limite, RepositoryCallback<List<TableroEntity>> callback) {
        executor.execute(() -> {
            try {
                List<TableroEntity> topClientes = tableroDao.obtenerTopClientesPorVisitas(limite);
                if (callback != null) callback.onSuccess(topClientes);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void obtenerDistribucionNiveles(RepositoryCallback<List<TableroDao.NivelFidelidadCount>> callback) {
        executor.execute(() -> {
            try {
                List<TableroDao.NivelFidelidadCount> distribucion = tableroDao.obtenerDistribucionNiveles();
                if (callback != null) callback.onSuccess(distribucion);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void obtenerSucursalesFavoritas(RepositoryCallback<List<TableroDao.SucursalFavoritaCount>> callback) {
        executor.execute(() -> {
            try {
                List<TableroDao.SucursalFavoritaCount> sucursales = tableroDao.obtenerSucursalesFavoritas();
                if (callback != null) callback.onSuccess(sucursales);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    // ==================== GESTIÓN DE CACHE ====================
    
    public void limpiarCacheExpirado(RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                Date fechaActual = new Date();
                tableroDao.limpiarCacheExpirado(fechaActual);
                
                successMessage.postValue("Cache limpiado");
                if (callback != null) callback.onSuccess(true);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al limpiar cache: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void invalidarCacheCliente(String clienteId, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                Date fechaActual = new Date();
                tableroDao.invalidarCache(clienteId, fechaActual);
                
                successMessage.postValue("Cache invalidado para cliente");
                if (callback != null) callback.onSuccess(true);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al invalidar cache: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void verificarEstadoCache(String clienteId, RepositoryCallback<EstadoCache> callback) {
        executor.execute(() -> {
            try {
                TableroEntity tablero = tableroDao.obtenerPorCliente(clienteId);
                EstadoCache estado = new EstadoCache();
                
                if (tablero != null) {
                    estado.existe = true;
                    estado.valido = !tablero.esCacheExpirado();
                    estado.sincronizado = tablero.isSincronizado();
                    estado.fechaActualizacion = tablero.getFechaActualizacion();
                    estado.fechaExpiracion = tablero.getFechaExpiracionCache();
                    estado.version = tablero.getVersion();
                } else {
                    estado.existe = false;
                }
                
                if (callback != null) callback.onSuccess(estado);
                
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    // ==================== SINCRONIZACIÓN EN SEGUNDO PLANO ====================
    
    private void refrescarTableroEnSegundoPlano(String clienteId) {
        executor.execute(() -> {
            obtenerTableroDesdeApi(clienteId, new RepositoryCallback<TableroEntity>() {
                @Override
                public void onSuccess(TableroEntity tableroApi) {
                    executor.execute(() -> {
                        try {
                            tableroApi.actualizarCache();
                            tableroApi.marcarComoSincronizado();
                            tableroDao.insertar(tableroApi);
                            dataSource.postValue("mixed");
                        } catch (Exception e) {
                            // Error silencioso en segundo plano
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    // Error silencioso en segundo plano
                    isOfflineMode.postValue(true);
                }
            });
        });
    }
    
    private void sincronizarTableroEnSegundoPlano(TableroEntity tablero) {
        executor.execute(() -> {
            // Implementar sincronización silenciosa con API
            try {
                Thread.sleep(2000); // Simular latencia
                tablero.marcarComoSincronizado();
                tableroDao.actualizar(tablero);
            } catch (Exception e) {
                // Error silencioso
            }
        });
    }
    
    // ==================== MÉTODOS PRIVADOS DE API ====================
    
    private void obtenerTableroDesdeApi(String clienteId, RepositoryCallback<TableroEntity> callback) {
        // Simular llamada a API
        executor.execute(() -> {
            try {
                Thread.sleep(1500); // Simular latencia de red
                
                // Crear tablero simulado
                TableroEntity tablero = new TableroEntity(clienteId, "Cliente " + clienteId);
                tablero.setTotalVisitas(25);
                tablero.setVisitasMesActual(8);
                tablero.setVisitasSemanaActual(3);
                tablero.setUltimaVisita(new Date());
                tablero.setPuntosTotales(1250);
                tablero.setPuntosDisponibles(850);
                tablero.setPuntosCanjeados(400);
                tablero.setBeneficiosDisponibles(12);
                tablero.setBeneficiosCanjeables(5);
                tablero.setTotalCanjes(8);
                tablero.setCanjesMesActual(2);
                tablero.setNivelFidelidad("Plata");
                tablero.setPuntosSiguienteNivel(150);
                tablero.setProgresoNivel(75.0);
                tablero.setRachaVisitas(5);
                tablero.setMetaVisitasMes(10);
                tablero.setProgresoMetaVisitas(80.0);
                tablero.setBeneficioRecomendadoNombre("Café Gratis");
                tablero.setBeneficioRecomendadoPuntos(100);
                tablero.setSucursalFavoritaNombre("Sucursal Centro");
                
                if (callback != null) callback.onSuccess(tablero);
                
            } catch (Exception e) {
                if (callback != null) callback.onError("Error de conexión: " + e.getMessage());
            }
        });
    }
    
    // ==================== CLASES AUXILIARES ====================
    
    public static class MetricasGlobales {
        public int totalVisitasGlobal;
        public int totalPuntosGlobal;
        public int totalCanjesGlobal;
        public double promedioVisitas;
        public double promedioPuntos;
        public double promedioCanjes;
        public int clientesActivos;
        public int clientesInactivos;
    }
    
    public static class EstadoCache {
        public boolean existe;
        public boolean valido;
        public boolean sincronizado;
        public Date fechaActualizacion;
        public Date fechaExpiracion;
        public long version;
    }
    
    // ==================== INTERFACE CALLBACK ====================
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    // ==================== LIMPIEZA DE RECURSOS ====================
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}