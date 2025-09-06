package com.example.cafefidelidaqrdemo.data.repositories;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.cafefidelidaqrdemo.data.dao.ReporteDao;
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class ReportesRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ReporteDao reporteDao;

    @Mock
    private ApiService apiService;

    @Mock
    private Call<List<ApiService.ReporteAdmin>> mockCall;

    @Mock
    private Call<ApiService.ReporteMetricas> mockMetricasCall;

    @Mock
    private Call<ApiService.ExportResponse> mockExportCall;

    private ReportesRepository repository;
    private List<ReporteEntity> testReportes;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ReportesRepository(reporteDao, apiService);
        
        // Datos de prueba
        testReportes = Arrays.asList(
            createTestReporte("1", "VISITAS", "2024-01-01", 100, 50, 25, 1500.0),
            createTestReporte("2", "CANJES", "2024-01-02", 80, 40, 20, 1200.0),
            createTestReporte("3", "CLIENTES", "2024-01-03", 120, 60, 30, 1800.0)
        );
    }

    private ReporteEntity createTestReporte(String id, String tipo, String fecha, 
                                          int visitas, int canjes, int clientes, double valor) {
        ReporteEntity reporte = new ReporteEntity();
        reporte.setId(id);
        reporte.setTipo(tipo);
        reporte.setFechaGeneracion(fecha);
        reporte.setTotalVisitas(visitas);
        reporte.setTotalCanjes(canjes);
        reporte.setClientesUnicos(clientes);
        reporte.setValorTotalCanjes(valor);
        reporte.setSucursalId("sucursal1");
        reporte.setBeneficioId("beneficio1");
        return reporte;
    }

    @Test
    public void testGetAllReportes() throws InterruptedException {
        // Arrange
        when(reporteDao.getAllReportes()).thenReturn(createLiveData(testReportes));

        // Act
        LiveData<List<ReporteEntity>> result = repository.getAllReportes();
        
        // Assert
        assertNotNull(result);
        List<ReporteEntity> reportes = getValueFromLiveData(result);
        assertEquals(3, reportes.size());
        assertEquals("1", reportes.get(0).getId());
        verify(reporteDao).getAllReportes();
    }

    @Test
    public void testGetReportesByTipo() throws InterruptedException {
        // Arrange
        List<ReporteEntity> reportesVisitas = Arrays.asList(testReportes.get(0));
        when(reporteDao.getReportesByTipo("VISITAS")).thenReturn(createLiveData(reportesVisitas));

        // Act
        LiveData<List<ReporteEntity>> result = repository.getReportesByTipo("VISITAS");
        
        // Assert
        assertNotNull(result);
        List<ReporteEntity> reportes = getValueFromLiveData(result);
        assertEquals(1, reportes.size());
        assertEquals("VISITAS", reportes.get(0).getTipo());
        verify(reporteDao).getReportesByTipo("VISITAS");
    }

    @Test
    public void testGetReportesByFechas() throws InterruptedException {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-02";
        List<ReporteEntity> reportesFiltrados = Arrays.asList(testReportes.get(0), testReportes.get(1));
        when(reporteDao.getReportesByFechas(fechaInicio, fechaFin))
            .thenReturn(createLiveData(reportesFiltrados));

        // Act
        LiveData<List<ReporteEntity>> result = repository.getReportesByFechas(fechaInicio, fechaFin);
        
        // Assert
        assertNotNull(result);
        List<ReporteEntity> reportes = getValueFromLiveData(result);
        assertEquals(2, reportes.size());
        verify(reporteDao).getReportesByFechas(fechaInicio, fechaFin);
    }

    @Test
    public void testGetReportesBySucursal() throws InterruptedException {
        // Arrange
        String sucursalId = "sucursal1";
        when(reporteDao.getReportesBySucursal(sucursalId))
            .thenReturn(createLiveData(testReportes));

        // Act
        LiveData<List<ReporteEntity>> result = repository.getReportesBySucursal(sucursalId);
        
        // Assert
        assertNotNull(result);
        List<ReporteEntity> reportes = getValueFromLiveData(result);
        assertEquals(3, reportes.size());
        verify(reporteDao).getReportesBySucursal(sucursalId);
    }

    @Test
    public void testInsertReporte() {
        // Arrange
        ReporteEntity nuevoReporte = createTestReporte("4", "NUEVO", "2024-01-04", 90, 45, 22, 1350.0);

        // Act
        repository.insertReporte(nuevoReporte);

        // Assert
        verify(reporteDao).insertReporte(nuevoReporte);
    }

    @Test
    public void testUpdateReporte() {
        // Arrange
        ReporteEntity reporteActualizado = testReportes.get(0);
        reporteActualizado.setTotalVisitas(150);

        // Act
        repository.updateReporte(reporteActualizado);

        // Assert
        verify(reporteDao).updateReporte(reporteActualizado);
    }

    @Test
    public void testDeleteReporte() {
        // Arrange
        ReporteEntity reporteAEliminar = testReportes.get(0);

        // Act
        repository.deleteReporte(reporteAEliminar);

        // Assert
        verify(reporteDao).deleteReporte(reporteAEliminar);
    }

    @Test
    public void testSyncWithApi_Success() throws InterruptedException {
        // Arrange
        List<ApiService.ReporteAdmin> apiReportes = Arrays.asList(
            createApiReporte("api1", "VISITAS", 200, 100, 50, 3000.0),
            createApiReporte("api2", "CANJES", 150, 75, 35, 2250.0)
        );
        
        when(apiService.getReportesAdmin(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockCall);
        
        doAnswer(invocation -> {
            Callback<List<ApiService.ReporteAdmin>> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(apiReportes));
            return null;
        }).when(mockCall).enqueue(any(Callback.class));

        // Act
        CountDownLatch latch = new CountDownLatch(1);
        repository.syncWithApi("2024-01-01", "2024-01-31", null, null, new ReportesRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("No debería fallar: " + error);
            }
        });

        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(apiService).getReportesAdmin("2024-01-01", "2024-01-31", null, null);
        verify(reporteDao, times(2)).insertReporte(any(ReporteEntity.class));
    }

    @Test
    public void testSyncWithApi_Failure() throws InterruptedException {
        // Arrange
        when(apiService.getReportesAdmin(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockCall);
        
        doAnswer(invocation -> {
            Callback<List<ApiService.ReporteAdmin>> callback = invocation.getArgument(0);
            callback.onFailure(mockCall, new Exception("Error de red"));
            return null;
        }).when(mockCall).enqueue(any(Callback.class));

        // Act
        CountDownLatch latch = new CountDownLatch(1);
        repository.syncWithApi("2024-01-01", "2024-01-31", null, null, new ReportesRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                fail("No debería tener éxito");
            }

            @Override
            public void onError(String error) {
                assertEquals("Error de red", error);
                latch.countDown();
            }
        });

        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testExportToCsv_Success() throws InterruptedException {
        // Arrange
        ApiService.ExportResponse exportResponse = new ApiService.ExportResponse();
        exportResponse.setSuccess(true);
        exportResponse.setDownloadUrl("http://example.com/report.csv");
        exportResponse.setFileName("reporte_2024.csv");
        
        when(apiService.exportReportesCsv(any(ApiService.ExportRequest.class)))
            .thenReturn(mockExportCall);
        
        doAnswer(invocation -> {
            Callback<ApiService.ExportResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockExportCall, Response.success(exportResponse));
            return null;
        }).when(mockExportCall).enqueue(any(Callback.class));

        // Act
        CountDownLatch latch = new CountDownLatch(1);
        repository.exportToCsv("VISITAS", "2024-01-01", "2024-01-31", null, null, 
            new ReportesRepository.ExportCallback() {
                @Override
                public void onSuccess(String downloadUrl, String fileName) {
                    assertEquals("http://example.com/report.csv", downloadUrl);
                    assertEquals("reporte_2024.csv", fileName);
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    fail("No debería fallar: " + error);
                }
            });

        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(apiService).exportReportesCsv(any(ApiService.ExportRequest.class));
    }

    @Test
    public void testGetMetricas_Success() throws InterruptedException {
        // Arrange
        ApiService.ReporteMetricas metricas = new ApiService.ReporteMetricas();
        metricas.setTotalVisitas(500);
        metricas.setTotalCanjes(250);
        metricas.setTotalClientes(100);
        metricas.setValorTotalCanjes(7500.0);
        
        when(apiService.getReportesMetricas(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockMetricasCall);
        
        doAnswer(invocation -> {
            Callback<ApiService.ReporteMetricas> callback = invocation.getArgument(0);
            callback.onResponse(mockMetricasCall, Response.success(metricas));
            return null;
        }).when(mockMetricasCall).enqueue(any(Callback.class));

        // Act
        CountDownLatch latch = new CountDownLatch(1);
        repository.getMetricas("2024-01-01", "2024-01-31", null, null, 
            new ReportesRepository.MetricasCallback() {
                @Override
                public void onSuccess(ApiService.ReporteMetricas result) {
                    assertEquals(500, result.getTotalVisitas());
                    assertEquals(250, result.getTotalCanjes());
                    assertEquals(100, result.getTotalClientes());
                    assertEquals(7500.0, result.getValorTotalCanjes(), 0.01);
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    fail("No debería fallar: " + error);
                }
            });

        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(apiService).getReportesMetricas("2024-01-01", "2024-01-31", null, null);
    }

    @Test
    public void testClearCache() {
        // Act
        repository.clearCache();

        // Assert
        verify(reporteDao).deleteAll();
    }

    @Test
    public void testGetCacheSize() throws InterruptedException {
        // Arrange
        when(reporteDao.getCount()).thenReturn(createLiveData(5));

        // Act
        LiveData<Integer> result = repository.getCacheSize();
        
        // Assert
        assertNotNull(result);
        Integer count = getValueFromLiveData(result);
        assertEquals(Integer.valueOf(5), count);
        verify(reporteDao).getCount();
    }

    // Métodos auxiliares
    private ApiService.ReporteAdmin createApiReporte(String id, String tipo, int visitas, 
                                                   int canjes, int clientes, double valor) {
        ApiService.ReporteAdmin reporte = new ApiService.ReporteAdmin();
        reporte.setId(id);
        reporte.setTipo(tipo);
        reporte.setTotalVisitas(visitas);
        reporte.setTotalCanjes(canjes);
        reporte.setClientesUnicos(clientes);
        reporte.setValorTotalCanjes(valor);
        return reporte;
    }

    private <T> LiveData<T> createLiveData(T value) {
        return new LiveData<T>() {
            {
                setValue(value);
            }
        };
    }

    private <T> T getValueFromLiveData(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        latch.await(2, TimeUnit.SECONDS);
        return (T) data[0];
    }
}