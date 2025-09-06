package com.example.cafefidelidaqrdemo.data.dao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cafefidelidaqrdemo.data.database.AppDatabase;
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ReporteDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase database;
    private ReporteDao reporteDao;

    @Mock
    private Observer<List<ReporteEntity>> listObserver;

    @Mock
    private Observer<ReporteEntity> entityObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase.class
        ).allowMainThreadQueries().build();
        
        reporteDao = database.reporteDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testInsertarReporte() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        
        // Act
        reporteDao.insertar(reporte);
        
        // Assert
        ReporteEntity reporteObtenido = reporteDao.obtenerPorId("reporte_001");
        assertNotNull(reporteObtenido);
        assertEquals("reporte_001", reporteObtenido.getId());
        assertEquals("VISITAS", reporteObtenido.getTipo());
    }

    @Test
    public void testInsertarMultiplesReportes() {
        // Arrange
        List<ReporteEntity> reportes = Arrays.asList(
            crearReportePrueba("reporte_001", "VISITAS", "2024-01-15"),
            crearReportePrueba("reporte_002", "CANJES", "2024-01-16"),
            crearReportePrueba("reporte_003", "PUNTOS", "2024-01-17")
        );
        
        // Act
        reporteDao.insertarTodos(reportes);
        
        // Assert
        List<ReporteEntity> reportesObtenidos = getValue(reporteDao.obtenerTodos());
        assertEquals(3, reportesObtenidos.size());
    }

    @Test
    public void testActualizarReporte() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        reporteDao.insertar(reporte);
        
        // Act
        reporte.setTotalVisitas(200);
        reporte.setTotalCanjes(50);
        reporteDao.actualizar(reporte);
        
        // Assert
        ReporteEntity reporteActualizado = reporteDao.obtenerPorId("reporte_001");
        assertEquals(200, reporteActualizado.getTotalVisitas());
        assertEquals(50, reporteActualizado.getTotalCanjes());
    }

    @Test
    public void testEliminarReporte() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        reporteDao.insertar(reporte);
        
        // Act
        reporteDao.eliminar(reporte);
        
        // Assert
        ReporteEntity reporteEliminado = reporteDao.obtenerPorId("reporte_001");
        assertNull(reporteEliminado);
    }

    @Test
    public void testEliminarPorId() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        reporteDao.insertar(reporte);
        
        // Act
        reporteDao.eliminarPorId("reporte_001");
        
        // Assert
        ReporteEntity reporteEliminado = reporteDao.obtenerPorId("reporte_001");
        assertNull(reporteEliminado);
    }

    @Test
    public void testObtenerPorTipo() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteEntity> reportesVisitas = getValue(reporteDao.obtenerPorTipo("VISITAS"));
        
        // Assert
        assertEquals(2, reportesVisitas.size());
        for (ReporteEntity reporte : reportesVisitas) {
            assertEquals("VISITAS", reporte.getTipo());
        }
    }

    @Test
    public void testObtenerPorRangoFechas() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteEntity> reportesEnero = getValue(
            reporteDao.obtenerPorRangoFechas("2024-01-01", "2024-01-31")
        );
        
        // Assert
        assertTrue(reportesEnero.size() >= 2);
        for (ReporteEntity reporte : reportesEnero) {
            assertTrue(reporte.getFechaGeneracion().startsWith("2024-01"));
        }
    }

    @Test
    public void testObtenerPorSucursal() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteEntity> reportesSucursal = getValue(
            reporteDao.obtenerPorSucursal("sucursal_001")
        );
        
        // Assert
        assertTrue(reportesSucursal.size() >= 1);
        for (ReporteEntity reporte : reportesSucursal) {
            assertEquals("sucursal_001", reporte.getSucursalId());
        }
    }

    @Test
    public void testObtenerPorBeneficio() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteEntity> reportesBeneficio = getValue(
            reporteDao.obtenerPorBeneficio("beneficio_001")
        );
        
        // Assert
        assertTrue(reportesBeneficio.size() >= 1);
        for (ReporteEntity reporte : reportesBeneficio) {
            assertEquals("beneficio_001", reporte.getBeneficioId());
        }
    }

    @Test
    public void testObtenerConFiltros() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteEntity> reportesFiltrados = getValue(
            reporteDao.obtenerConFiltros("VISITAS", "2024-01-01", "2024-01-31", 
                "sucursal_001", "beneficio_001")
        );
        
        // Assert
        assertNotNull(reportesFiltrados);
        for (ReporteEntity reporte : reportesFiltrados) {
            assertEquals("VISITAS", reporte.getTipo());
            assertEquals("sucursal_001", reporte.getSucursalId());
            assertEquals("beneficio_001", reporte.getBeneficioId());
            assertTrue(reporte.getFechaGeneracion().startsWith("2024-01"));
        }
    }

    @Test
    public void testObtenerEstadisticasGenerales() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        ReporteDao.EstadisticasGenerales estadisticas = reporteDao.obtenerEstadisticasGenerales();
        
        // Assert
        assertNotNull(estadisticas);
        assertTrue(estadisticas.totalVisitas > 0);
        assertTrue(estadisticas.totalCanjes > 0);
        assertTrue(estadisticas.clientesUnicos > 0);
        assertTrue(estadisticas.valorTotalCanjes > 0);
    }

    @Test
    public void testObtenerEstadisticasPorTipo() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteDao.EstadisticasPorTipo> estadisticasTipo = 
            reporteDao.obtenerEstadisticasPorTipo();
        
        // Assert
        assertNotNull(estadisticasTipo);
        assertFalse(estadisticasTipo.isEmpty());
        
        for (ReporteDao.EstadisticasPorTipo estadistica : estadisticasTipo) {
            assertNotNull(estadistica.tipo);
            assertTrue(estadistica.totalReportes > 0);
        }
    }

    @Test
    public void testObtenerEstadisticasPorSucursal() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteDao.EstadisticasPorSucursal> estadisticasSucursal = 
            reporteDao.obtenerEstadisticasPorSucursal();
        
        // Assert
        assertNotNull(estadisticasSucursal);
        assertFalse(estadisticasSucursal.isEmpty());
        
        for (ReporteDao.EstadisticasPorSucursal estadistica : estadisticasSucursal) {
            assertNotNull(estadistica.sucursalId);
            assertTrue(estadistica.totalVisitas >= 0);
        }
    }

    @Test
    public void testObtenerTopClientesPorCanjes() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteDao.TopCliente> topClientes = reporteDao.obtenerTopClientesPorCanjes(5);
        
        // Assert
        assertNotNull(topClientes);
        assertTrue(topClientes.size() <= 5);
        
        // Verificar que están ordenados por canjes descendente
        for (int i = 1; i < topClientes.size(); i++) {
            assertTrue(topClientes.get(i-1).totalCanjes >= topClientes.get(i).totalCanjes);
        }
    }

    @Test
    public void testObtenerTopClientesPorVisitas() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteDao.TopCliente> topClientes = reporteDao.obtenerTopClientesPorVisitas(3);
        
        // Assert
        assertNotNull(topClientes);
        assertTrue(topClientes.size() <= 3);
        
        // Verificar que están ordenados por visitas descendente
        for (int i = 1; i < topClientes.size(); i++) {
            assertTrue(topClientes.get(i-1).totalVisitas >= topClientes.get(i).totalVisitas);
        }
    }

    @Test
    public void testObtenerTendenciaTemporal() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteDao.TendenciaTemporal> tendencia = 
            reporteDao.obtenerTendenciaTemporal("2024-01-01", "2024-02-28");
        
        // Assert
        assertNotNull(tendencia);
        assertFalse(tendencia.isEmpty());
        
        for (ReporteDao.TendenciaTemporal punto : tendencia) {
            assertNotNull(punto.fecha);
            assertTrue(punto.totalVisitas >= 0);
            assertTrue(punto.totalCanjes >= 0);
        }
    }

    @Test
    public void testObtenerRendimientoPorSucursal() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        List<ReporteDao.RendimientoSucursal> rendimiento = 
            reporteDao.obtenerRendimientoPorSucursal("2024-01-01", "2024-01-31");
        
        // Assert
        assertNotNull(rendimiento);
        assertFalse(rendimiento.isEmpty());
        
        for (ReporteDao.RendimientoSucursal sucursal : rendimiento) {
            assertNotNull(sucursal.sucursalId);
            assertTrue(sucursal.totalVisitas >= 0);
            assertTrue(sucursal.totalCanjes >= 0);
            assertTrue(sucursal.valorTotalCanjes >= 0);
        }
    }

    @Test
    public void testObtenerReportesPendientesSincronizacion() {
        // Arrange
        ReporteEntity reportePendiente = crearReportePrueba("reporte_pendiente", "VISITAS", "2024-01-15");
        reportePendiente.setEstadoSincronizacion("PENDIENTE");
        reporteDao.insertar(reportePendiente);
        
        ReporteEntity reporteSincronizado = crearReportePrueba("reporte_sincronizado", "CANJES", "2024-01-16");
        reporteSincronizado.setEstadoSincronizacion("SINCRONIZADO");
        reporteDao.insertar(reporteSincronizado);
        
        // Act
        List<ReporteEntity> reportesPendientes = getValue(
            reporteDao.obtenerReportesPendientesSincronizacion()
        );
        
        // Assert
        assertEquals(1, reportesPendientes.size());
        assertEquals("reporte_pendiente", reportesPendientes.get(0).getId());
        assertEquals("PENDIENTE", reportesPendientes.get(0).getEstadoSincronizacion());
    }

    @Test
    public void testObtenerReportesConError() {
        // Arrange
        ReporteEntity reporteError = crearReportePrueba("reporte_error", "VISITAS", "2024-01-15");
        reporteError.setEstadoSincronizacion("ERROR");
        reporteDao.insertar(reporteError);
        
        ReporteEntity reporteOk = crearReportePrueba("reporte_ok", "CANJES", "2024-01-16");
        reporteOk.setEstadoSincronizacion("SINCRONIZADO");
        reporteDao.insertar(reporteOk);
        
        // Act
        List<ReporteEntity> reportesError = getValue(reporteDao.obtenerReportesConError());
        
        // Assert
        assertEquals(1, reportesError.size());
        assertEquals("reporte_error", reportesError.get(0).getId());
        assertEquals("ERROR", reportesError.get(0).getEstadoSincronizacion());
    }

    @Test
    public void testMarcarComoSincronizado() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        reporte.setEstadoSincronizacion("PENDIENTE");
        reporteDao.insertar(reporte);
        
        // Act
        reporteDao.marcarComoSincronizado("reporte_001");
        
        // Assert
        ReporteEntity reporteActualizado = reporteDao.obtenerPorId("reporte_001");
        assertEquals("SINCRONIZADO", reporteActualizado.getEstadoSincronizacion());
        assertTrue(reporteActualizado.getUltimaSincronizacion() > 0);
    }

    @Test
    public void testMarcarComoError() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        reporte.setEstadoSincronizacion("PENDIENTE");
        reporteDao.insertar(reporte);
        
        String mensajeError = "Error de conexión";
        
        // Act
        reporteDao.marcarComoError("reporte_001", mensajeError);
        
        // Assert
        ReporteEntity reporteActualizado = reporteDao.obtenerPorId("reporte_001");
        assertEquals("ERROR", reporteActualizado.getEstadoSincronizacion());
        assertEquals(mensajeError, reporteActualizado.getMensajeError());
    }

    @Test
    public void testLimpiarCacheAntiguo() {
        // Arrange
        long ahora = System.currentTimeMillis();
        long hace2Horas = ahora - (2 * 60 * 60 * 1000);
        
        ReporteEntity reporteAntiguo = crearReportePrueba("reporte_antiguo", "VISITAS", "2024-01-15");
        reporteAntiguo.setUltimaModificacion(hace2Horas);
        reporteDao.insertar(reporteAntiguo);
        
        ReporteEntity reporteReciente = crearReportePrueba("reporte_reciente", "CANJES", "2024-01-16");
        reporteReciente.setUltimaModificacion(ahora);
        reporteDao.insertar(reporteReciente);
        
        // Act
        int eliminados = reporteDao.limpiarCacheAntiguo(60 * 60 * 1000); // 1 hora
        
        // Assert
        assertEquals(1, eliminados);
        assertNull(reporteDao.obtenerPorId("reporte_antiguo"));
        assertNotNull(reporteDao.obtenerPorId("reporte_reciente"));
    }

    @Test
    public void testContarReportes() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        int totalReportes = reporteDao.contarReportes();
        
        // Assert
        assertTrue(totalReportes > 0);
    }

    @Test
    public void testContarReportesPorTipo() {
        // Arrange
        insertarReportesPrueba();
        
        // Act
        int reportesVisitas = reporteDao.contarReportesPorTipo("VISITAS");
        
        // Assert
        assertTrue(reportesVisitas > 0);
    }

    @Test
    public void testExisteReporte() {
        // Arrange
        ReporteEntity reporte = crearReportePrueba("reporte_001", "VISITAS", "2024-01-15");
        reporteDao.insertar(reporte);
        
        // Act & Assert
        assertTrue(reporteDao.existeReporte("reporte_001"));
        assertFalse(reporteDao.existeReporte("reporte_inexistente"));
    }

    // Métodos auxiliares
    private ReporteEntity crearReportePrueba(String id, String tipo, String fecha) {
        ReporteEntity reporte = new ReporteEntity();
        reporte.setId(id);
        reporte.setTipo(tipo);
        reporte.setFechaGeneracion(fecha);
        reporte.setTotalVisitas(100);
        reporte.setTotalCanjes(25);
        reporte.setClientesUnicos(50);
        reporte.setValorTotalCanjes(1250.0);
        reporte.setSucursalId("sucursal_001");
        reporte.setBeneficioId("beneficio_001");
        reporte.setEstadoSincronizacion("PENDIENTE");
        reporte.setVersion(1);
        reporte.setUltimaModificacion(System.currentTimeMillis());
        return reporte;
    }

    private void insertarReportesPrueba() {
        List<ReporteEntity> reportes = Arrays.asList(
            crearReportePrueba("reporte_001", "VISITAS", "2024-01-15"),
            crearReportePrueba("reporte_002", "CANJES", "2024-01-16"),
            crearReportePrueba("reporte_003", "VISITAS", "2024-01-17"),
            crearReportePrueba("reporte_004", "PUNTOS", "2024-02-01")
        );
        
        reporteDao.insertarTodos(reportes);
    }

    // Método auxiliar para obtener valores de LiveData en tests
    private <T> T getValue(LiveData<T> liveData) {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                data[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        
        liveData.observeForever(observer);
        
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        return (T) data[0];
    }
}