package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.data.repositories.ReportesRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class ReportesAdminViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ReportesRepository reportesRepository;

    @Mock
    private Observer<List<ReporteEntity>> reportesObserver;

    @Mock
    private Observer<ReportesAdminViewModel.EstadoCarga> estadoObserver;

    @Mock
    private Observer<String> mensajeObserver;

    @Mock
    private Observer<Boolean> booleanObserver;

    @Mock
    private Observer<ReportesAdminViewModel.FiltrosReporte> filtrosObserver;

    @Mock
    private Observer<ReportesAdminViewModel.MetricasReporte> metricasObserver;

    private ReportesAdminViewModel viewModel;
    private MutableLiveData<List<ReporteEntity>> reportesLiveData;
    private MutableLiveData<ReportesRepository.EstadoCarga> estadoCargaLiveData;
    private MutableLiveData<String> mensajeErrorLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        reportesLiveData = new MutableLiveData<>();
        estadoCargaLiveData = new MutableLiveData<>();
        mensajeErrorLiveData = new MutableLiveData<>();
        
        // Configurar mocks del repositorio
        when(reportesRepository.obtenerReportesFiltrados(any(), any(), any(), any()))
            .thenReturn(reportesLiveData);
        when(reportesRepository.getEstadoCarga()).thenReturn(estadoCargaLiveData);
        when(reportesRepository.getMensajeError()).thenReturn(mensajeErrorLiveData);
        
        viewModel = new ReportesAdminViewModel(reportesRepository);
    }

    @Test
    public void testInicializacionViewModel() {
        // Assert
        assertNotNull(viewModel.getReportes());
        assertNotNull(viewModel.getEstadoCarga());
        assertNotNull(viewModel.getMensajeError());
        assertNotNull(viewModel.getFiltrosActuales());
        assertNotNull(viewModel.getMetricasActuales());
        assertNotNull(viewModel.isExportandoCSV());
        assertNotNull(viewModel.isActualizandoAutomaticamente());
    }

    @Test
    public void testObtenerReportes() {
        // Arrange
        List<ReporteEntity> reportesEsperados = Arrays.asList(
            crearReportePrueba("reporte_001", "VENTAS"),
            crearReportePrueba("reporte_002", "VISITAS")
        );
        
        // Act
        LiveData<List<ReporteEntity>> reportes = viewModel.getReportes();
        reportes.observeForever(reportesObserver);
        reportesLiveData.setValue(reportesEsperados);
        
        // Assert
        verify(reportesObserver).onChanged(reportesEsperados);
    }

    @Test
    public void testAplicarFiltros() {
        // Arrange
        String tipoReporte = "VENTAS";
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String sucursalId = "sucursal_001";
        
        // Act
        viewModel.aplicarFiltros(tipoReporte, fechaInicio, fechaFin, sucursalId);
        
        // Assert
        verify(reportesRepository).obtenerReportesFiltrados(tipoReporte, fechaInicio, fechaFin, sucursalId);
        
        // Verificar que los filtros se actualizaron
        LiveData<ReportesAdminViewModel.FiltrosReporte> filtros = viewModel.getFiltrosActuales();
        filtros.observeForever(filtrosObserver);
        
        ReportesAdminViewModel.FiltrosReporte filtrosEsperados = 
            new ReportesAdminViewModel.FiltrosReporte(tipoReporte, fechaInicio, fechaFin, sucursalId);
        
        // Simular actualización de filtros
        viewModel.actualizarFiltros(filtrosEsperados);
        verify(filtrosObserver).onChanged(any(ReportesAdminViewModel.FiltrosReporte.class));
    }

    @Test
    public void testLimpiarFiltros() {
        // Arrange
        viewModel.aplicarFiltros("VENTAS", "2024-01-01", "2024-01-31", "sucursal_001");
        
        // Act
        viewModel.limpiarFiltros();
        
        // Assert
        verify(reportesRepository, atLeastOnce()).obtenerReportesFiltrados(null, null, null, null);
    }

    @Test
    public void testActualizarReportes() {
        // Act
        viewModel.actualizarReportes();
        
        // Assert
        verify(reportesRepository).sincronizarReportes();
    }

    @Test
    public void testExportarCSV_Exitoso() throws InterruptedException {
        // Arrange
        List<ReporteEntity> reportes = Arrays.asList(
            crearReportePrueba("reporte_001", "VENTAS")
        );
        String nombreArchivo = "reportes_ventas_2024.csv";
        
        CountDownLatch latch = new CountDownLatch(1);
        
        // Simular exportación exitosa
        doAnswer(invocation -> {
            ReportesRepository.CallbackExportacion callback = invocation.getArgument(2);
            callback.onExportacionExitosa("Archivo exportado: " + nombreArchivo);
            latch.countDown();
            return null;
        }).when(reportesRepository).exportarCSV(eq(reportes), eq(nombreArchivo), any());
        
        // Act
        viewModel.exportarCSV(reportes, nombreArchivo);
        
        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        verify(reportesRepository).exportarCSV(eq(reportes), eq(nombreArchivo), any());
        
        // Verificar estado de exportación
        LiveData<Boolean> exportando = viewModel.isExportandoCSV();
        exportando.observeForever(booleanObserver);
    }

    @Test
    public void testExportarCSV_Error() throws InterruptedException {
        // Arrange
        List<ReporteEntity> reportes = Arrays.asList(
            crearReportePrueba("reporte_001", "VENTAS")
        );
        String nombreArchivo = "reportes_ventas_2024.csv";
        String mensajeError = "Error al exportar archivo";
        
        CountDownLatch latch = new CountDownLatch(1);
        
        // Simular error en exportación
        doAnswer(invocation -> {
            ReportesRepository.CallbackExportacion callback = invocation.getArgument(2);
            callback.onErrorExportacion(mensajeError);
            latch.countDown();
            return null;
        }).when(reportesRepository).exportarCSV(eq(reportes), eq(nombreArchivo), any());
        
        // Act
        viewModel.exportarCSV(reportes, nombreArchivo);
        
        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        verify(reportesRepository).exportarCSV(eq(reportes), eq(nombreArchivo), any());
    }

    @Test
    public void testCalcularMetricas() {
        // Arrange
        List<ReporteEntity> reportes = Arrays.asList(
            crearReportePrueba("reporte_001", "VENTAS"),
            crearReportePrueba("reporte_002", "VISITAS"),
            crearReportePrueba("reporte_003", "CANJES")
        );
        
        // Configurar datos de prueba
        reportes.get(0).setTotalVentas(15000.0);
        reportes.get(0).setTotalVisitas(300);
        reportes.get(0).setTotalCanjes(45);
        
        reportes.get(1).setTotalVentas(12000.0);
        reportes.get(1).setTotalVisitas(250);
        reportes.get(1).setTotalCanjes(38);
        
        reportes.get(2).setTotalVentas(18000.0);
        reportes.get(2).setTotalVisitas(350);
        reportes.get(2).setTotalCanjes(52);
        
        // Act
        viewModel.calcularMetricas(reportes);
        
        // Assert
        LiveData<ReportesAdminViewModel.MetricasReporte> metricas = viewModel.getMetricasActuales();
        metricas.observeForever(metricasObserver);
        
        verify(metricasObserver).onChanged(any(ReportesAdminViewModel.MetricasReporte.class));
    }

    @Test
    public void testObtenerReportesPorTipo() {
        // Arrange
        String tipoReporte = "VENTAS";
        
        // Act
        viewModel.obtenerReportesPorTipo(tipoReporte);
        
        // Assert
        verify(reportesRepository).obtenerReportesPorTipo(tipoReporte);
    }

    @Test
    public void testObtenerReportesPorFecha() {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        
        // Act
        viewModel.obtenerReportesPorFecha(fechaInicio, fechaFin);
        
        // Assert
        verify(reportesRepository).obtenerReportesPorFecha(fechaInicio, fechaFin);
    }

    @Test
    public void testObtenerReportesPorSucursal() {
        // Arrange
        String sucursalId = "sucursal_001";
        
        // Act
        viewModel.obtenerReportesPorSucursal(sucursalId);
        
        // Assert
        verify(reportesRepository).obtenerReportesPorSucursal(sucursalId);
    }

    @Test
    public void testIniciarActualizacionAutomatica() {
        // Act
        viewModel.iniciarActualizacionAutomatica(30000); // 30 segundos
        
        // Assert
        LiveData<Boolean> actualizando = viewModel.isActualizandoAutomaticamente();
        actualizando.observeForever(booleanObserver);
        
        // Verificar que se inició la actualización automática
        assertTrue(viewModel.isActualizacionAutomaticaActiva());
    }

    @Test
    public void testDetenerActualizacionAutomatica() {
        // Arrange
        viewModel.iniciarActualizacionAutomatica(30000);
        
        // Act
        viewModel.detenerActualizacionAutomatica();
        
        // Assert
        assertFalse(viewModel.isActualizacionAutomaticaActiva());
    }

    @Test
    public void testManejarEstadoCarga() {
        // Arrange
        ReportesRepository.EstadoCarga estadoCargando = ReportesRepository.EstadoCarga.CARGANDO;
        ReportesRepository.EstadoCarga estadoExitoso = ReportesRepository.EstadoCarga.EXITOSO;
        ReportesRepository.EstadoCarga estadoError = ReportesRepository.EstadoCarga.ERROR;
        
        // Act & Assert
        LiveData<ReportesAdminViewModel.EstadoCarga> estado = viewModel.getEstadoCarga();
        estado.observeForever(estadoObserver);
        
        // Simular cambios de estado
        estadoCargaLiveData.setValue(estadoCargando);
        estadoCargaLiveData.setValue(estadoExitoso);
        estadoCargaLiveData.setValue(estadoError);
        
        verify(estadoObserver, times(3)).onChanged(any(ReportesAdminViewModel.EstadoCarga.class));
    }

    @Test
    public void testManejarMensajesError() {
        // Arrange
        String mensajeError = "Error al cargar reportes";
        
        // Act
        LiveData<String> mensaje = viewModel.getMensajeError();
        mensaje.observeForever(mensajeObserver);
        mensajeErrorLiveData.setValue(mensajeError);
        
        // Assert
        verify(mensajeObserver).onChanged(mensajeError);
    }

    @Test
    public void testValidarFiltros_FiltrosValidos() {
        // Arrange
        String tipoReporte = "VENTAS";
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String sucursalId = "sucursal_001";
        
        // Act
        boolean sonValidos = viewModel.validarFiltros(tipoReporte, fechaInicio, fechaFin, sucursalId);
        
        // Assert
        assertTrue(sonValidos);
    }

    @Test
    public void testValidarFiltros_FechaInvalida() {
        // Arrange
        String tipoReporte = "VENTAS";
        String fechaInicio = "2024-01-31";
        String fechaFin = "2024-01-01"; // Fecha fin anterior a fecha inicio
        String sucursalId = "sucursal_001";
        
        // Act
        boolean sonValidos = viewModel.validarFiltros(tipoReporte, fechaInicio, fechaFin, sucursalId);
        
        // Assert
        assertFalse(sonValidos);
    }

    @Test
    public void testObtenerOpcionesReporte() {
        // Act
        viewModel.obtenerOpcionesReporte();
        
        // Assert
        verify(reportesRepository).obtenerOpcionesReporte();
    }

    @Test
    public void testLimpiarCache() {
        // Act
        viewModel.limpiarCache();
        
        // Assert
        verify(reportesRepository).limpiarCache();
    }

    @Test
    public void testOnCleared() {
        // Act
        viewModel.onCleared();
        
        // Assert - Verificar que se detuvo la actualización automática
        assertFalse(viewModel.isActualizacionAutomaticaActiva());
    }

    @Test
    public void testFiltrosReporte_Constructor() {
        // Arrange
        String tipo = "VENTAS";
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        String sucursal = "sucursal_001";
        
        // Act
        ReportesAdminViewModel.FiltrosReporte filtros = 
            new ReportesAdminViewModel.FiltrosReporte(tipo, fechaInicio, fechaFin, sucursal);
        
        // Assert
        assertEquals(tipo, filtros.getTipoReporte());
        assertEquals(fechaInicio, filtros.getFechaInicio());
        assertEquals(fechaFin, filtros.getFechaFin());
        assertEquals(sucursal, filtros.getSucursalId());
    }

    @Test
    public void testMetricasReporte_Constructor() {
        // Arrange
        int totalReportes = 150;
        double ventasPromedio = 15000.0;
        int visitasPromedio = 300;
        double tasaConversion = 0.15;
        
        // Act
        ReportesAdminViewModel.MetricasReporte metricas = 
            new ReportesAdminViewModel.MetricasReporte(totalReportes, ventasPromedio, visitasPromedio, tasaConversion);
        
        // Assert
        assertEquals(totalReportes, metricas.getTotalReportes());
        assertEquals(ventasPromedio, metricas.getVentasPromedio(), 0.01);
        assertEquals(visitasPromedio, metricas.getVisitasPromedio());
        assertEquals(tasaConversion, metricas.getTasaConversion(), 0.01);
    }

    @Test
    public void testEstadoCarga_Valores() {
        // Assert
        assertNotNull(ReportesAdminViewModel.EstadoCarga.INICIAL);
        assertNotNull(ReportesAdminViewModel.EstadoCarga.CARGANDO);
        assertNotNull(ReportesAdminViewModel.EstadoCarga.EXITOSO);
        assertNotNull(ReportesAdminViewModel.EstadoCarga.ERROR);
        assertNotNull(ReportesAdminViewModel.EstadoCarga.VACIO);
    }

    @Test
    public void testManejoMemoria_LiberarRecursos() {
        // Arrange
        viewModel.iniciarActualizacionAutomatica(30000);
        
        // Act
        viewModel.liberarRecursos();
        
        // Assert
        assertFalse(viewModel.isActualizacionAutomaticaActiva());
        verify(reportesRepository).cancelarOperacionesEnCurso();
    }

    @Test
    public void testManejoOffline_CacheLocal() {
        // Arrange
        when(reportesRepository.tieneConexion()).thenReturn(false);
        
        // Act
        viewModel.manejarModoOffline();
        
        // Assert
        verify(reportesRepository).obtenerReportesCache();
    }

    @Test
    public void testSincronizacionPendiente() {
        // Act
        viewModel.sincronizarCambiosPendientes();
        
        // Assert
        verify(reportesRepository).sincronizarReportesPendientes();
    }

    // Métodos auxiliares
    private ReporteEntity crearReportePrueba(String id, String tipo) {
        ReporteEntity reporte = new ReporteEntity();
        reporte.setId(id);
        reporte.setTipoReporte(tipo);
        reporte.setFechaInicio("2024-01-01");
        reporte.setFechaFin("2024-01-31");
        reporte.setSucursalId("sucursal_001");
        reporte.setTotalVentas(15000.0);
        reporte.setTotalVisitas(300);
        reporte.setTotalCanjes(45);
        reporte.setTotalClientes(120);
        reporte.setEstadoSincronizacion("SINCRONIZADO");
        reporte.setVersion(1);
        reporte.setUltimaModificacion(System.currentTimeMillis());
        return reporte;
    }
}