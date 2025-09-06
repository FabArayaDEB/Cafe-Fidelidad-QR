package com.example.cafefidelidaqrdemo.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.dao.ReporteDao;
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.database.models.MetricasVisitas;
import com.example.cafefidelidaqrdemo.database.models.MetricasCanjes;
import com.example.cafefidelidaqrdemo.database.models.TopCliente;
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
 * Repository para gestionar datos de reportes administrativos
 * Maneja filtros, sincronización y cache de reportes
 */
public class ReportesRepository {
    
    private final ReporteDao reporteDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para estados observables
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> syncProgress = new MutableLiveData<>(0);
    
    public ReportesRepository(Context context, ReporteDao reporteDao) {
        this.context = context;
        this.reporteDao = reporteDao;
        this.apiService = ApiClient.getApiService();
        this.executor = Executors.newFixedThreadPool(3);
    }
    
    // ==================== GETTERS PARA LIVEDATA ====================
    
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getIsSyncing() { return isSyncing; }
    public LiveData<Integer> getSyncProgress() { return syncProgress; }
    
    // ==================== OPERACIONES BÁSICAS ====================
    
    public LiveData<List<ReporteEntity>> obtenerTodosLosReportes() {
        return reporteDao.obtenerTodosLiveData();
    }
    
    public LiveData<ReporteEntity> obtenerReportePorId(String reporteId) {
        return reporteDao.obtenerPorIdLiveData(reporteId);
    }
    
    public void insertarReporte(ReporteEntity reporte, RepositoryCallback<ReporteEntity> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Validar datos del reporte
                if (!validarReporte(reporte)) {
                    errorMessage.postValue("Datos del reporte inválidos");
                    if (callback != null) callback.onError("Datos inválidos");
                    return;
                }
                
                // Verificar duplicados
                ReporteEntity existente = reporteDao.obtenerPorId(reporte.getId());
                if (existente != null) {
                    errorMessage.postValue("Ya existe un reporte con este ID");
                    if (callback != null) callback.onError("Reporte duplicado");
                    return;
                }
                
                // Insertar en base de datos local
                reporteDao.insertar(reporte);
                
                // Sincronizar con API si hay conexión
                sincronizarReporteConApi(reporte, new RepositoryCallback<ReporteEntity>() {
                    @Override
                    public void onSuccess(ReporteEntity result) {
                        successMessage.postValue("Reporte creado y sincronizado");
                        if (callback != null) callback.onSuccess(result);
                    }
                    
                    @Override
                    public void onError(String error) {
                        successMessage.postValue("Reporte creado localmente (pendiente sincronización)");
                        if (callback != null) callback.onSuccess(reporte);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al crear reporte: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void actualizarReporte(ReporteEntity reporte, RepositoryCallback<ReporteEntity> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Validar datos del reporte
                if (!validarReporte(reporte)) {
                    errorMessage.postValue("Datos del reporte inválidos");
                    if (callback != null) callback.onError("Datos inválidos");
                    return;
                }
                
                // Verificar que existe
                ReporteEntity existente = reporteDao.obtenerPorId(reporte.getId());
                if (existente == null) {
                    errorMessage.postValue("Reporte no encontrado");
                    if (callback != null) callback.onError("Reporte no encontrado");
                    return;
                }
                
                // Control de versiones
                if (existente.getVersion() > reporte.getVersion()) {
                    errorMessage.postValue("Conflicto de versiones. El reporte ha sido modificado por otro usuario.");
                    if (callback != null) callback.onError("Conflicto de versiones");
                    return;
                }
                
                // Incrementar versión y actualizar
                reporte.incrementarVersion();
                reporteDao.actualizar(reporte);
                
                // Sincronizar con API
                sincronizarReporteConApi(reporte, new RepositoryCallback<ReporteEntity>() {
                    @Override
                    public void onSuccess(ReporteEntity result) {
                        successMessage.postValue("Reporte actualizado y sincronizado");
                        if (callback != null) callback.onSuccess(result);
                    }
                    
                    @Override
                    public void onError(String error) {
                        successMessage.postValue("Reporte actualizado localmente (pendiente sincronización)");
                        if (callback != null) callback.onSuccess(reporte);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar reporte: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void eliminarReporte(String reporteId, RepositoryCallback<Boolean> callback) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Verificar que existe
                ReporteEntity reporte = reporteDao.obtenerPorId(reporteId);
                if (reporte == null) {
                    errorMessage.postValue("Reporte no encontrado");
                    if (callback != null) callback.onError("Reporte no encontrado");
                    return;
                }
                
                // Eliminar de base de datos local
                reporteDao.eliminar(reporte);
                
                // Eliminar de API si está sincronizado
                if (reporte.isSincronizado()) {
                    eliminarReporteDeApi(reporteId, new RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            successMessage.postValue("Reporte eliminado completamente");
                            if (callback != null) callback.onSuccess(true);
                        }
                        
                        @Override
                        public void onError(String error) {
                            successMessage.postValue("Reporte eliminado localmente");
                            if (callback != null) callback.onSuccess(true);
                        }
                    });
                } else {
                    successMessage.postValue("Reporte eliminado");
                    if (callback != null) callback.onSuccess(true);
                }
                
            } catch (Exception e) {
                errorMessage.postValue("Error al eliminar reporte: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    // ==================== CONSULTAS CON FILTROS ====================
    
    public LiveData<List<ReporteEntity>> obtenerReportesPorTipo(String tipo) {
        return reporteDao.obtenerPorTipoLiveData(tipo);
    }
    
    public LiveData<List<ReporteEntity>> obtenerReportesPorRangoFechas(Date fechaInicio, Date fechaFin) {
        return reporteDao.obtenerPorRangoFechasLiveData(fechaInicio, fechaFin);
    }
    
    public LiveData<List<ReporteEntity>> obtenerReportesPorSucursal(Long sucursalId) {
        return reporteDao.obtenerPorSucursalLiveData(sucursalId);
    }
    
    public LiveData<List<ReporteEntity>> obtenerReportesPorBeneficio(String beneficioId) {
        return reporteDao.obtenerPorBeneficioLiveData(beneficioId);
    }
    
    public LiveData<List<ReporteEntity>> obtenerReportesConFiltros(String tipo, Date fechaInicio, Date fechaFin, Long sucursalId, String beneficioId) {
        return reporteDao.obtenerConFiltrosLiveData(tipo, fechaInicio, fechaFin, sucursalId, beneficioId);
    }
    
    // ==================== MÉTRICAS Y ESTADÍSTICAS ====================
    
    public void obtenerMetricasVisitas(Date fechaInicio, Date fechaFin, Long sucursalId, RepositoryCallback<MetricasVisitas> callback) {
        executor.execute(() -> {
            try {
                MetricasVisitas metricas = reporteDao.obtenerMetricasVisitas(fechaInicio, fechaFin, sucursalId);
                if (callback != null) callback.onSuccess(metricas);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void obtenerMetricasCanjes(Date fechaInicio, Date fechaFin, String beneficioId, RepositoryCallback<MetricasCanjes> callback) {
        executor.execute(() -> {
            try {
                MetricasCanjes metricas = reporteDao.obtenerMetricasCanjes(fechaInicio, fechaFin, beneficioId);
                if (callback != null) callback.onSuccess(metricas);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    public void obtenerTopClientes(int limite, Date fechaInicio, Date fechaFin, RepositoryCallback<List<TopCliente>> callback) {
        executor.execute(() -> {
            try {
                List<TopCliente> topClientes = reporteDao.obtenerTopClientes(limite, fechaInicio, fechaFin);
                if (callback != null) callback.onSuccess(topClientes);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    // ==================== SINCRONIZACIÓN ====================
    
    public void sincronizarTodosLosReportes(RepositoryCallback<Boolean> callback) {
        isSyncing.postValue(true);
        syncProgress.postValue(0);
        
        executor.execute(() -> {
            try {
                // Obtener reportes desde API
                obtenerReportesDesdeApi(new RepositoryCallback<List<ReporteEntity>>() {
                    @Override
                    public void onSuccess(List<ReporteEntity> reportesApi) {
                        executor.execute(() -> {
                            try {
                                // Sincronizar reportes locales no sincronizados
                                List<ReporteEntity> reportesLocales = reporteDao.obtenerNoSincronizados();
                                int total = reportesApi.size() + reportesLocales.size();
                                int procesados = 0;
                                
                                // Actualizar reportes desde API
                                for (ReporteEntity reporteApi : reportesApi) {
                                    ReporteEntity reporteLocal = reporteDao.obtenerPorId(reporteApi.getId());
                                    if (reporteLocal == null || reporteLocal.getVersion() < reporteApi.getVersion()) {
                                        reporteApi.marcarComoSincronizado();
                                        reporteDao.insertar(reporteApi);
                                    }
                                    procesados++;
                                    syncProgress.postValue((procesados * 100) / total);
                                }
                                
                                // Enviar reportes locales a API
                                for (ReporteEntity reporteLocal : reportesLocales) {
                                    sincronizarReporteConApi(reporteLocal, null);
                                    procesados++;
                                    syncProgress.postValue((procesados * 100) / total);
                                }
                                
                                syncProgress.postValue(100);
                                successMessage.postValue("Sincronización completada");
                                if (callback != null) callback.onSuccess(true);
                                
                            } catch (Exception e) {
                                errorMessage.postValue("Error en sincronización: " + e.getMessage());
                                if (callback != null) callback.onError(e.getMessage());
                            } finally {
                                isSyncing.postValue(false);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al obtener reportes del servidor: " + error);
                        if (callback != null) callback.onError(error);
                        isSyncing.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
                isSyncing.postValue(false);
            }
        });
    }
    
    // ==================== EXPORTACIÓN ====================
    
    public void exportarReportesCSV(List<ReporteEntity> reportes, RepositoryCallback<String> callback) {
        executor.execute(() -> {
            try {
                StringBuilder csv = new StringBuilder();
                
                // Encabezados
                csv.append("ID,Tipo,Fecha Inicio,Fecha Fin,Sucursal,Beneficio,Total Visitas,Total Canjes,Valor Total,Promedio Visitas,Promedio Canjes\n");
                
                // Datos
                for (ReporteEntity reporte : reportes) {
                    csv.append(reporte.getId()).append(",")
                        .append(reporte.getTipoReporte()).append(",")
                       .append(reporte.getFechaInicio()).append(",")
                       .append(reporte.getFechaFin()).append(",")
                       .append(reporte.getSucursalId() != null ? reporte.getSucursalId() : "").append(",")
                       .append(reporte.getBeneficioId() != null ? reporte.getBeneficioId() : "").append(",")
                       .append(reporte.getTotalVisitas()).append(",")
                       .append(reporte.getTotalCanjes()).append(",")
                       .append(reporte.getValorTotalCanjes()).append(",")
                       .append(reporte.getPromedioVisitasDia()).append(",")
                       .append(reporte.getPromedioCanjesDia()).append("\n");
                }
                
                if (callback != null) callback.onSuccess(csv.toString());
                
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    private boolean validarReporte(ReporteEntity reporte) {
        return reporte != null &&
               reporte.getId() != null && !reporte.getId().trim().isEmpty() &&
               reporte.getTipoReporte() != null && !reporte.getTipoReporte().trim().isEmpty() &&
               reporte.getFechaInicio() != null &&
               reporte.getFechaFin() != null &&
               reporte.getFechaInicio().before(reporte.getFechaFin());
    }
    
    private void sincronizarReporteConApi(ReporteEntity reporte, RepositoryCallback<ReporteEntity> callback) {
        // Implementar llamada a API para sincronizar reporte
        // Por ahora simulamos éxito
        executor.execute(() -> {
            try {
                Thread.sleep(1000); // Simular latencia de red
                reporte.marcarComoSincronizado();
                reporteDao.actualizar(reporte);
                if (callback != null) callback.onSuccess(reporte);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    private void obtenerReportesDesdeApi(RepositoryCallback<List<ReporteEntity>> callback) {
        // Implementar llamada a API para obtener reportes
        // Por ahora simulamos respuesta vacía
        executor.execute(() -> {
            try {
                Thread.sleep(1000); // Simular latencia de red
                if (callback != null) callback.onSuccess(java.util.Collections.emptyList());
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
    
    private void eliminarReporteDeApi(String reporteId, RepositoryCallback<Boolean> callback) {
        // Implementar llamada a API para eliminar reporte
        // Por ahora simulamos éxito
        executor.execute(() -> {
            try {
                Thread.sleep(1000); // Simular latencia de red
                if (callback != null) callback.onSuccess(true);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
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