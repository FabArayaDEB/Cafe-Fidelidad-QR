package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.repository.ReportesRepository;
import com.example.cafefidelidaqrdemo.database.dao.ReporteDao;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.models.TopCliente;
import java.util.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para la gestión de reportes administrativos
 * Maneja filtros, exportación y visualización de métricas
 */
public class ReportesAdminViewModel extends AndroidViewModel {
    
    private final ReportesRepository reportesRepository;
    private final ExecutorService executorService;
    
    // Estados de la UI
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOfflineMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> dataSource = new MutableLiveData<>();
    
    // Datos de reportes
    private final MutableLiveData<List<ReporteEntity>> reportes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ReporteMetricas> metricas = new MutableLiveData<>();
    private final MutableLiveData<List<TopCliente>> topClientes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<RendimientoSucursal>> rendimientoSucursales = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<TendenciaTemporal>> tendencias = new MutableLiveData<>(new ArrayList<>());
    
    // Filtros activos
    private final MutableLiveData<FiltrosReporte> filtrosActivos = new MutableLiveData<>(new FiltrosReporte());
    private final MutableLiveData<List<String>> tiposDisponibles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> sucursalesDisponibles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> beneficiosDisponibles = new MutableLiveData<>(new ArrayList<>());
    
    // Estado de exportación
    private final MutableLiveData<Boolean> isExporting = new MutableLiveData<>(false);
    private final MutableLiveData<String> exportProgress = new MutableLiveData<>();
    private final MutableLiveData<String> exportResult = new MutableLiveData<>();
    
    public ReportesAdminViewModel(@NonNull Application application) {
        super(application);
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        this.reportesRepository = new ReportesRepository(application, database.reporteDao());
        this.executorService = Executors.newFixedThreadPool(3);
        
        // Inicializar filtros por defecto
        FiltrosReporte filtrosDefault = new FiltrosReporte();
        filtrosDefault.fechaInicio = getStartOfMonth();
        filtrosDefault.fechaFin = new Date();
        filtrosActivos.setValue(filtrosDefault);
        
        // Cargar datos iniciales
        cargarDatosIniciales();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getIsOfflineMode() { return isOfflineMode; }
    public LiveData<String> getDataSource() { return dataSource; }
    
    public LiveData<List<ReporteEntity>> getReportes() { return reportes; }
    public LiveData<ReporteMetricas> getMetricas() { return metricas; }
    public LiveData<List<TopCliente>> getTopClientes() { return topClientes; }
    public LiveData<List<RendimientoSucursal>> getRendimientoSucursales() { return rendimientoSucursales; }
    public LiveData<List<TendenciaTemporal>> getTendencias() { return tendencias; }
    
    public LiveData<FiltrosReporte> getFiltrosActivos() { return filtrosActivos; }
    public LiveData<List<String>> getTiposDisponibles() { return tiposDisponibles; }
    public LiveData<List<String>> getSucursalesDisponibles() { return sucursalesDisponibles; }
    public LiveData<List<String>> getBeneficiosDisponibles() { return beneficiosDisponibles; }
    
    public LiveData<Boolean> getIsExporting() { return isExporting; }
    public LiveData<String> getExportProgress() { return exportProgress; }
    public LiveData<String> getExportResult() { return exportResult; }
    
    // Métodos principales
    public void cargarDatosIniciales() {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                
                // Cargar opciones de filtros con valores por defecto
                List<String> tipos = Arrays.asList("VISITAS", "CANJES", "TOP_CLIENTES", "RENDIMIENTO");
                List<String> sucursales = Arrays.asList("Sucursal 1", "Sucursal 2", "Sucursal 3");
                List<String> beneficios = Arrays.asList("Descuento 10%", "Producto Gratis", "2x1");
                
                tiposDisponibles.postValue(tipos);
                sucursalesDisponibles.postValue(sucursales);
                beneficiosDisponibles.postValue(beneficios);
                
                // Cargar reportes con filtros por defecto
                aplicarFiltros(filtrosActivos.getValue());
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar datos iniciales: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void aplicarFiltros(FiltrosReporte filtros) {
        if (filtros == null) return;
        
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                filtrosActivos.postValue(filtros);
                
                // Cargar reportes filtrados usando LiveData
                LiveData<List<ReporteEntity>> reportesLiveData = reportesRepository.obtenerReportesConFiltros(
                    filtros.tipo, filtros.fechaInicio, filtros.fechaFin, 
                    filtros.sucursalId != null ? Long.valueOf(filtros.sucursalId) : null, 
                    filtros.beneficioId
                );
                
                // Observar los cambios y procesar los datos
                reportesLiveData.observeForever(reportesFiltrados -> {
                    if (reportesFiltrados != null) {
                        reportes.postValue(reportesFiltrados);
                        
                        // Calcular métricas
                        ReporteMetricas metricasCalculadas = calcularMetricas(reportesFiltrados, filtros);
                        metricas.postValue(metricasCalculadas);
                        
                        // Cargar datos adicionales
                        cargarTopClientes(filtros);
                        cargarRendimientoSucursales(filtros);
                        cargarTendencias(filtros);
                    }
                });
                
                dataSource.postValue("api");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al aplicar filtros: " + e.getMessage());
                // Intentar cargar desde cache
                cargarDesdeCache(filtros);
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void refrescarReportes() {
        FiltrosReporte filtros = filtrosActivos.getValue();
        if (filtros != null) {
            // Forzar actualización desde API
            executorService.execute(() -> {
                try {
                    isLoading.postValue(true);
                    reportesRepository.sincronizarTodosLosReportes(new ReportesRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // Sincronización exitosa
            }
            
            @Override
            public void onError(String error) {
                // Error en sincronización
            }
        });
                    aplicarFiltros(filtros);
                } catch (Exception e) {
                    errorMessage.postValue("Error al refrescar: " + e.getMessage());
                } finally {
                    isLoading.postValue(false);
                }
            });
        }
    }
    
    public void exportarCSV(String nombreArchivo) {
        FiltrosReporte filtros = filtrosActivos.getValue();
        List<ReporteEntity> reportesActuales = reportes.getValue();
        
        if (filtros == null || reportesActuales == null || reportesActuales.isEmpty()) {
            errorMessage.setValue("No hay datos para exportar");
            return;
        }
        
        executorService.execute(() -> {
            try {
                isExporting.postValue(true);
                exportProgress.postValue("Preparando exportación...");
                
                reportesRepository.exportarReportesCSV(
                    reportesActuales,
                    new ReportesRepository.RepositoryCallback<String>() {
                        @Override
                        public void onSuccess(String rutaArchivo) {
                            exportProgress.postValue("Exportación completada");
                            exportResult.postValue(rutaArchivo);
                            successMessage.postValue("Reporte exportado exitosamente");
                        }
                        
                        @Override
                        public void onError(String error) {
                            errorMessage.postValue("Error al exportar: " + error);
                        }
                    }
                );
                
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar: " + e.getMessage());
            } finally {
                isExporting.postValue(false);
            }
        });
    }
    
    public void limpiarFiltros() {
        FiltrosReporte filtrosLimpios = new FiltrosReporte();
        filtrosLimpios.fechaInicio = getStartOfMonth();
        filtrosLimpios.fechaFin = new Date();
        aplicarFiltros(filtrosLimpios);
    }
    
    // Métodos auxiliares
    private void cargarDesdeCache(FiltrosReporte filtros) {
        try {
            // Usar obtenerReportesConFiltros en lugar de obtenerReportesCache
            LiveData<List<ReporteEntity>> reportesLiveData = reportesRepository.obtenerReportesConFiltros(
                filtros.tipo, filtros.fechaInicio, filtros.fechaFin, 
                filtros.sucursalId != null ? Long.valueOf(filtros.sucursalId) : null, 
                filtros.beneficioId
            );
            
            reportesLiveData.observeForever(reportesCache -> {
                if (reportesCache != null && !reportesCache.isEmpty()) {
                    reportes.postValue(reportesCache);
                    ReporteMetricas metricasCache = calcularMetricas(reportesCache, filtros);
                    metricas.postValue(metricasCache);
                    dataSource.postValue("cache");
                    isOfflineMode.postValue(true);
                } else {
                    errorMessage.postValue("No hay datos disponibles offline");
                }
            });
        } catch (Exception e) {
            errorMessage.postValue("Error al cargar datos offline: " + e.getMessage());
        }
    }
    
    private void cargarTopClientes(FiltrosReporte filtros) {
        try {
            // Usar obtenerTopClientes con callback
            reportesRepository.obtenerTopClientes(
                10, filtros.fechaInicio, filtros.fechaFin,
                new ReportesRepository.RepositoryCallback<List<TopCliente>>() {
                    @Override
                    public void onSuccess(List<TopCliente> top) {
                        topClientes.postValue(top);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Error silencioso para datos secundarios
                    }
                }
            );
        } catch (Exception e) {
            // Error silencioso para datos secundarios
        }
    }
    
    private void cargarRendimientoSucursales(FiltrosReporte filtros) {
        executorService.execute(() -> {
            try {
                // Crear datos de ejemplo para rendimiento de sucursales
                List<RendimientoSucursal> rendimiento = Arrays.asList(
                    new RendimientoSucursal("1", "Sucursal Centro", 150, 75, 2250.0, 45),
                    new RendimientoSucursal("2", "Sucursal Norte", 120, 60, 1800.0, 38),
                    new RendimientoSucursal("3", "Sucursal Sur", 100, 50, 1500.0, 32)
                );
                rendimientoSucursales.postValue(rendimiento);
            } catch (Exception e) {
                // Error silencioso para datos secundarios
            }
        });
    }
    
    private void cargarTendencias(FiltrosReporte filtros) {
        try {
            // Crear datos de ejemplo para tendencias temporales
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.JANUARY, 1);
            Date fecha1 = cal.getTime();
            cal.set(2024, Calendar.FEBRUARY, 1);
            Date fecha2 = cal.getTime();
            cal.set(2024, Calendar.MARCH, 1);
            Date fecha3 = cal.getTime();
            
            List<TendenciaTemporal> tendenciasData = Arrays.asList(
                new TendenciaTemporal(fecha1, 45, 23, 690.0),
                new TendenciaTemporal(fecha2, 52, 28, 840.0),
                new TendenciaTemporal(fecha3, 48, 25, 750.0)
            );
            tendencias.postValue(tendenciasData);
        } catch (Exception e) {
            // Error silencioso para datos secundarios
        }
    }
    
    private ReporteMetricas calcularMetricas(List<ReporteEntity> reportesList, FiltrosReporte filtros) {
        ReporteMetricas metricas = new ReporteMetricas();
        
        if (reportesList.isEmpty()) {
            return metricas;
        }
        
        // Calcular métricas básicas
        metricas.totalVisitas = reportesList.stream()
            .mapToInt(ReporteEntity::getTotalVisitas)
            .sum();
            
        metricas.totalCanjes = reportesList.stream()
            .mapToInt(ReporteEntity::getTotalCanjes)
            .sum();
            
        metricas.totalClientes = reportesList.stream()
            .mapToInt(ReporteEntity::getTotalClientesActivos)
            .sum();
            
        metricas.valorTotalCanjes = reportesList.stream()
            .mapToDouble(ReporteEntity::getValorTotalCanjes)
            .sum();
        
        // Calcular promedios
        int diasPeriodo = calcularDiasPeriodo(filtros.fechaInicio, filtros.fechaFin);
        if (diasPeriodo > 0) {
            metricas.promedioVisitasDiarias = (double) metricas.totalVisitas / diasPeriodo;
            metricas.promedioCanjesDiarios = (double) metricas.totalCanjes / diasPeriodo;
        }
        
        if (metricas.totalClientes > 0) {
            metricas.promedioVisitasPorCliente = (double) metricas.totalVisitas / metricas.totalClientes;
            metricas.promedioCanjesPorCliente = (double) metricas.totalCanjes / metricas.totalClientes;
        }
        
        // Calcular tasas
        if (metricas.totalVisitas > 0) {
            metricas.tasaConversion = ((double) metricas.totalCanjes / metricas.totalVisitas) * 100;
        }
        
        if (metricas.totalCanjes > 0) {
            metricas.valorPromedioCanje = metricas.valorTotalCanjes / metricas.totalCanjes;
        }
        
        return metricas;
    }
    
    private Date getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private int calcularDiasPeriodo(Date inicio, Date fin) {
        if (inicio == null || fin == null) return 0;
        long diffInMillies = Math.abs(fin.getTime() - inicio.getTime());
        return (int) (diffInMillies / (24 * 60 * 60 * 1000)) + 1;
    }
    
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (reportesRepository != null) {
            reportesRepository.cleanup();
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        cleanup();
    }
    
    // Clases auxiliares
    public static class FiltrosReporte {
        public String tipo;
        public Date fechaInicio;
        public Date fechaFin;
        public String sucursalId;
        public String beneficioId;
        
        public FiltrosReporte() {}
        
        public FiltrosReporte(String tipo, Date fechaInicio, Date fechaFin, String sucursalId, String beneficioId) {
            this.tipo = tipo;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.sucursalId = sucursalId;
            this.beneficioId = beneficioId;
        }
        
        public boolean tieneRangoFechas() {
            return fechaInicio != null && fechaFin != null;
        }
        
        public boolean esFiltroCompleto() {
            return tipo != null || sucursalId != null || beneficioId != null || tieneRangoFechas();
        }
    }
    
    public static class ReporteMetricas {
        public int totalVisitas = 0;
        public int totalCanjes = 0;
        public int totalClientes = 0;
        public double valorTotalCanjes = 0.0;
        public double promedioVisitasDiarias = 0.0;
        public double promedioCanjesDiarios = 0.0;
        public double promedioVisitasPorCliente = 0.0;
        public double promedioCanjesPorCliente = 0.0;
        public double tasaConversion = 0.0;
        public double valorPromedioCanje = 0.0;
        
        public ReporteMetricas() {}
    }
    
    // Alias para compatibilidad
    public static class MetricasReporte extends ReporteMetricas {
        public MetricasReporte() {
            super();
        }
    }
    

    
    public static class TopClienteInfo {
        public String clienteId;
        public String nombre;
        public String email;
        public int totalVisitas;
        public int totalCanjes;
        public double valorTotalCanjes;
        public String sucursalFavorita;
        public Date ultimaVisita;
        public String telefono;
        
        public TopClienteInfo(String clienteId, String nombre, String email, int totalVisitas, int totalCanjes, double valorTotalCanjes, String sucursalFavorita, Date ultimaVisita, String telefono) {
            this.clienteId = clienteId;
            this.nombre = nombre;
            this.email = email;
            this.totalVisitas = totalVisitas;
            this.totalCanjes = totalCanjes;
            this.valorTotalCanjes = valorTotalCanjes;
            this.sucursalFavorita = sucursalFavorita;
            this.ultimaVisita = ultimaVisita;
            this.telefono = telefono;
        }
    }
    
    public static class RendimientoSucursal {
        public String sucursalId;
        public String nombre;
        public int totalVisitas;
        public int totalCanjes;
        public double valorTotalCanjes;
        public int clientesUnicos;
        public double tasaConversion;
        
        public RendimientoSucursal(String sucursalId, String nombre, int totalVisitas, int totalCanjes, double valorTotalCanjes, int clientesUnicos) {
            this.sucursalId = sucursalId;
            this.nombre = nombre;
            this.totalVisitas = totalVisitas;
            this.totalCanjes = totalCanjes;
            this.valorTotalCanjes = valorTotalCanjes;
            this.clientesUnicos = clientesUnicos;
            this.tasaConversion = totalVisitas > 0 ? ((double) totalCanjes / totalVisitas) * 100 : 0;
        }
    }
    
    public static class TendenciaTemporal {
        public Date fecha;
        public int visitas;
        public int canjes;
        public double valor;
        
        public TendenciaTemporal(Date fecha, int visitas, int canjes, double valor) {
            this.fecha = fecha;
            this.visitas = visitas;
            this.canjes = canjes;
            this.valor = valor;
        }
    }
}