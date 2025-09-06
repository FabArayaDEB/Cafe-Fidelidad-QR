package com.example.cafefidelidaqrdemo.data.repositories;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.cafefidelidaqrdemo.data.dao.TableroDao;
import com.example.cafefidelidaqrdemo.data.entities.TableroEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(JUnit4.class)
public class TableroRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private TableroDao tableroDao;

    @Mock
    private ApiService apiService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private Call<ApiService.TableroCliente> mockCall;

    @Mock
    private Call<List<ApiService.TableroCliente>> mockListCall;

    @Mock
    private Call<Void> mockVoidCall;

    @Mock
    private Observer<TableroEntity> entityObserver;

    @Mock
    private Observer<List<TableroEntity>> listObserver;

    @Mock
    private Observer<TableroRepository.EstadoCarga> estadoObserver;

    private TableroRepository repository;
    private MutableLiveData<TableroEntity> tableroLiveData;
    private MutableLiveData<List<TableroEntity>> tablerosLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        tableroLiveData = new MutableLiveData<>();
        tablerosLiveData = new MutableLiveData<>();
        
        // Configurar mocks del DAO
        when(tableroDao.obtenerPorCliente(anyString())).thenReturn(tableroLiveData);
        when(tableroDao.obtenerTodos()).thenReturn(tablerosLiveData);
        when(tableroDao.obtenerClientesActivos()).thenReturn(tablerosLiveData);
        
        // Configurar mock del ExecutorService para ejecutar inmediatamente
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).execute(any(Runnable.class));
        
        repository = new TableroRepository(tableroDao, apiService, executorService);
    }

    @Test
    public void testObtenerTableroCliente() {
        // Arrange
        String clienteId = "cliente_001";
        TableroEntity tableroEsperado = crearTableroPrueba("tablero_001", clienteId);
        
        // Act
        LiveData<TableroEntity> resultado = repository.obtenerTableroCliente(clienteId);
        resultado.observeForever(entityObserver);
        tableroLiveData.setValue(tableroEsperado);
        
        // Assert
        verify(tableroDao).obtenerPorCliente(clienteId);
        verify(entityObserver).onChanged(tableroEsperado);
    }

    @Test
    public void testObtenerTodosTableros() {
        // Arrange
        List<TableroEntity> tablerosEsperados = Arrays.asList(
            crearTableroPrueba("tablero_001", "cliente_001"),
            crearTableroPrueba("tablero_002", "cliente_002")
        );
        
        // Act
        LiveData<List<TableroEntity>> resultado = repository.obtenerTodosTableros();
        resultado.observeForever(listObserver);
        tablerosLiveData.setValue(tablerosEsperados);
        
        // Assert
        verify(tableroDao).obtenerTodos();
        verify(listObserver).onChanged(tablerosEsperados);
    }

    @Test
    public void testObtenerClientesActivos() {
        // Arrange
        List<TableroEntity> clientesActivos = Arrays.asList(
            crearTableroPrueba("tablero_001", "cliente_001")
        );
        
        // Act
        LiveData<List<TableroEntity>> resultado = repository.obtenerClientesActivos();
        resultado.observeForever(listObserver);
        tablerosLiveData.setValue(clientesActivos);
        
        // Assert
        verify(tableroDao).obtenerClientesActivos();
        verify(listObserver).onChanged(clientesActivos);
    }

    @Test
    public void testSincronizarTableroCliente_Exitoso() {
        // Arrange
        String clienteId = "cliente_001";
        ApiService.TableroCliente tableroApi = crearTableroApiPrueba();
        
        when(apiService.obtenerTableroCliente(clienteId)).thenReturn(mockCall);
        
        // Act
        repository.sincronizarTableroCliente(clienteId);
        
        // Capturar y ejecutar el callback
        ArgumentCaptor<Callback<ApiService.TableroCliente>> callbackCaptor = 
            ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(callbackCaptor.capture());
        
        Response<ApiService.TableroCliente> response = Response.success(tableroApi);
        callbackCaptor.getValue().onResponse(mockCall, response);
        
        // Assert
        verify(apiService).obtenerTableroCliente(clienteId);
        verify(tableroDao).insertar(any(TableroEntity.class));
    }

    @Test
    public void testSincronizarTableroCliente_Error() {
        // Arrange
        String clienteId = "cliente_001";
        when(apiService.obtenerTableroCliente(clienteId)).thenReturn(mockCall);
        
        // Act
        repository.sincronizarTableroCliente(clienteId);
        
        // Capturar y ejecutar el callback con error
        ArgumentCaptor<Callback<ApiService.TableroCliente>> callbackCaptor = 
            ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(callbackCaptor.capture());
        
        callbackCaptor.getValue().onFailure(mockCall, new Exception("Error de red"));
        
        // Assert
        verify(apiService).obtenerTableroCliente(clienteId);
        verify(tableroDao, never()).insertar(any(TableroEntity.class));
    }

    @Test
    public void testRefrescarTableroCliente_Exitoso() {
        // Arrange
        String clienteId = "cliente_001";
        ApiService.TableroCliente tableroApi = crearTableroApiPrueba();
        
        when(apiService.refrescarTableroCliente(clienteId)).thenReturn(mockCall);
        
        // Act
        repository.refrescarTableroCliente(clienteId);
        
        // Capturar y ejecutar el callback
        ArgumentCaptor<Callback<ApiService.TableroCliente>> callbackCaptor = 
            ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(callbackCaptor.capture());
        
        Response<ApiService.TableroCliente> response = Response.success(tableroApi);
        callbackCaptor.getValue().onResponse(mockCall, response);
        
        // Assert
        verify(apiService).refrescarTableroCliente(clienteId);
        verify(tableroDao).actualizar(any(TableroEntity.class));
    }

    @Test
    public void testCanjearBeneficio_Exitoso() {
        // Arrange
        String clienteId = "cliente_001";
        String beneficioId = "beneficio_001";
        int puntos = 500;
        
        ApiService.CanjeRequest request = new ApiService.CanjeRequest(clienteId, beneficioId, puntos);
        when(apiService.canjearBeneficio(any(ApiService.CanjeRequest.class))).thenReturn(mockVoidCall);
        
        // Act
        repository.canjearBeneficio(clienteId, beneficioId, puntos);
        
        // Capturar y ejecutar el callback
        ArgumentCaptor<Callback<Void>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockVoidCall).enqueue(callbackCaptor.capture());
        
        Response<Void> response = Response.success(null);
        callbackCaptor.getValue().onResponse(mockVoidCall, response);
        
        // Assert
        verify(apiService).canjearBeneficio(any(ApiService.CanjeRequest.class));
    }

    @Test
    public void testRegistrarVisita_Exitoso() {
        // Arrange
        String clienteId = "cliente_001";
        String sucursalId = "sucursal_001";
        
        ApiService.VisitaRequest request = new ApiService.VisitaRequest(clienteId, sucursalId);
        when(apiService.registrarVisita(any(ApiService.VisitaRequest.class))).thenReturn(mockVoidCall);
        
        // Act
        repository.registrarVisita(clienteId, sucursalId);
        
        // Capturar y ejecutar el callback
        ArgumentCaptor<Callback<Void>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockVoidCall).enqueue(callbackCaptor.capture());
        
        Response<Void> response = Response.success(null);
        callbackCaptor.getValue().onResponse(mockVoidCall, response);
        
        // Assert
        verify(apiService).registrarVisita(any(ApiService.VisitaRequest.class));
    }

    @Test
    public void testActualizarMeta_Exitoso() {
        // Arrange
        String clienteId = "cliente_001";
        String tipoMeta = "VISITAS";
        int valorMeta = 50;
        
        ApiService.MetaRequest request = new ApiService.MetaRequest(clienteId, tipoMeta, valorMeta);
        when(apiService.actualizarMeta(any(ApiService.MetaRequest.class))).thenReturn(mockVoidCall);
        
        // Act
        repository.actualizarMeta(clienteId, tipoMeta, valorMeta);
        
        // Capturar y ejecutar el callback
        ArgumentCaptor<Callback<Void>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockVoidCall).enqueue(callbackCaptor.capture());
        
        Response<Void> response = Response.success(null);
        callbackCaptor.getValue().onResponse(mockVoidCall, response);
        
        // Assert
        verify(apiService).actualizarMeta(any(ApiService.MetaRequest.class));
    }

    @Test
    public void testInsertarTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        
        // Act
        repository.insertarTablero(tablero);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).insertar(tablero);
    }

    @Test
    public void testActualizarTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        
        // Act
        repository.actualizarTablero(tablero);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).actualizar(tablero);
    }

    @Test
    public void testEliminarTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        
        // Act
        repository.eliminarTablero(tablero);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).eliminar(tablero);
    }

    @Test
    public void testLimpiarCache() {
        // Act
        repository.limpiarCache();
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).limpiarCacheAntiguo(anyLong());
    }

    @Test
    public void testSincronizarTablerosPendientes() {
        // Arrange
        List<TableroEntity> tablerosPendientes = Arrays.asList(
            crearTableroPrueba("tablero_001", "cliente_001")
        );
        tablerosPendientes.get(0).setEstadoSincronizacion("PENDIENTE");
        
        MutableLiveData<List<TableroEntity>> pendientesLiveData = new MutableLiveData<>();
        when(tableroDao.obtenerTablerosPendientesSincronizacion()).thenReturn(pendientesLiveData);
        
        // Act
        repository.sincronizarTablerosPendientes();
        pendientesLiveData.setValue(tablerosPendientes);
        
        // Assert
        verify(tableroDao).obtenerTablerosPendientesSincronizacion();
    }

    @Test
    public void testObtenerEstadisticasGenerales() {
        // Arrange
        TableroDao.EstadisticasGenerales estadisticas = new TableroDao.EstadisticasGenerales();
        estadisticas.totalClientes = 100;
        estadisticas.totalVisitas = 2500;
        estadisticas.totalPuntos = 125000;
        
        when(tableroDao.obtenerEstadisticasGenerales()).thenReturn(estadisticas);
        
        // Act
        repository.obtenerEstadisticasGenerales();
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).obtenerEstadisticasGenerales();
    }

    @Test
    public void testObtenerTopClientesPorVisitas() {
        // Arrange
        int limite = 10;
        List<TableroDao.TopCliente> topClientes = Arrays.asList(
            new TableroDao.TopCliente("cliente_001", "Cliente 1", 50, 2500, 15)
        );
        
        when(tableroDao.obtenerTopClientesPorVisitas(limite)).thenReturn(topClientes);
        
        // Act
        repository.obtenerTopClientesPorVisitas(limite);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).obtenerTopClientesPorVisitas(limite);
    }

    @Test
    public void testObtenerAnalisisActividad() {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-01-31";
        List<TableroDao.AnalisisActividad> analisis = Arrays.asList(
            new TableroDao.AnalisisActividad("cliente_001", "Cliente 1", 15, 5, 750)
        );
        
        when(tableroDao.obtenerAnalisisActividad(fechaInicio, fechaFin)).thenReturn(analisis);
        
        // Act
        repository.obtenerAnalisisActividad(fechaInicio, fechaFin);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).obtenerAnalisisActividad(fechaInicio, fechaFin);
    }

    @Test
    public void testManejarErrorSincronizacion() {
        // Arrange
        String tableroId = "tablero_001";
        String mensajeError = "Error de conexión";
        
        // Act
        repository.manejarErrorSincronizacion(tableroId, mensajeError);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).marcarComoError(tableroId, mensajeError);
    }

    @Test
    public void testMarcarComoSincronizado() {
        // Arrange
        String tableroId = "tablero_001";
        
        // Act
        repository.marcarComoSincronizado(tableroId);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).marcarComoSincronizado(tableroId);
    }

    @Test
    public void testValidarConectividad_ConConexion() {
        // Arrange
        when(apiService.verificarConectividad()).thenReturn(mockVoidCall);
        
        // Act
        boolean tieneConexion = repository.validarConectividad();
        
        // Capturar y ejecutar el callback
        ArgumentCaptor<Callback<Void>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockVoidCall).enqueue(callbackCaptor.capture());
        
        Response<Void> response = Response.success(null);
        callbackCaptor.getValue().onResponse(mockVoidCall, response);
        
        // Assert
        verify(apiService).verificarConectividad();
    }

    @Test
    public void testValidarConectividad_SinConexion() {
        // Arrange
        when(apiService.verificarConectividad()).thenReturn(mockVoidCall);
        
        // Act
        boolean tieneConexion = repository.validarConectividad();
        
        // Capturar y ejecutar el callback con error
        ArgumentCaptor<Callback<Void>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockVoidCall).enqueue(callbackCaptor.capture());
        
        callbackCaptor.getValue().onFailure(mockVoidCall, new Exception("Sin conexión"));
        
        // Assert
        verify(apiService).verificarConectividad();
    }

    @Test
    public void testObtenerEstadoCarga() {
        // Act
        LiveData<TableroRepository.EstadoCarga> estadoCarga = repository.getEstadoCarga();
        estadoCarga.observeForever(estadoObserver);
        
        // Assert
        assertNotNull(estadoCarga);
    }

    @Test
    public void testObtenerMensajeError() {
        // Act
        LiveData<String> mensajeError = repository.getMensajeError();
        
        // Assert
        assertNotNull(mensajeError);
    }

    @Test
    public void testConversionApiAEntity() {
        // Arrange
        ApiService.TableroCliente tableroApi = crearTableroApiPrueba();
        
        // Act
        TableroEntity tableroEntity = repository.convertirApiAEntity(tableroApi);
        
        // Assert
        assertNotNull(tableroEntity);
        assertEquals(tableroApi.clienteId, tableroEntity.getClienteId());
        assertEquals(tableroApi.totalVisitas, tableroEntity.getTotalVisitas());
        assertEquals(tableroApi.puntosActuales, tableroEntity.getPuntosActuales());
        assertEquals(tableroApi.beneficiosDisponibles, tableroEntity.getBeneficiosDisponibles());
        assertEquals(tableroApi.totalCanjes, tableroEntity.getTotalCanjes());
        assertEquals(tableroApi.nivelFidelidad, tableroEntity.getNivelFidelidad());
    }

    @Test
    public void testConversionEntityAApi() {
        // Arrange
        TableroEntity tableroEntity = crearTableroPrueba("tablero_001", "cliente_001");
        
        // Act
        ApiService.TableroCliente tableroApi = repository.convertirEntityAApi(tableroEntity);
        
        // Assert
        assertNotNull(tableroApi);
        assertEquals(tableroEntity.getClienteId(), tableroApi.clienteId);
        assertEquals(tableroEntity.getTotalVisitas(), tableroApi.totalVisitas);
        assertEquals(tableroEntity.getPuntosActuales(), tableroApi.puntosActuales);
        assertEquals(tableroEntity.getBeneficiosDisponibles(), tableroApi.beneficiosDisponibles);
        assertEquals(tableroEntity.getTotalCanjes(), tableroApi.totalCanjes);
        assertEquals(tableroEntity.getNivelFidelidad(), tableroApi.nivelFidelidad);
    }

    @Test
    public void testManejoCache_Expiracion() {
        // Arrange
        long tiempoExpiracion = 60 * 60 * 1000; // 1 hora
        
        // Act
        repository.configurarTiempoExpiracionCache(tiempoExpiracion);
        repository.limpiarCacheExpirado();
        
        // Assert
        verify(executorService, atLeastOnce()).execute(any(Runnable.class));
        verify(tableroDao).limpiarCacheAntiguo(tiempoExpiracion);
    }

    @Test
    public void testManejoOffline_GuardarCambiosPendientes() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tablero.setEstadoSincronizacion("PENDIENTE");
        
        // Act
        repository.guardarCambioOffline(tablero);
        
        // Assert
        verify(executorService).execute(any(Runnable.class));
        verify(tableroDao).actualizar(tablero);
        assertEquals("PENDIENTE", tablero.getEstadoSincronizacion());
    }

    // Métodos auxiliares
    private TableroEntity crearTableroPrueba(String id, String clienteId) {
        TableroEntity tablero = new TableroEntity();
        tablero.setId(id);
        tablero.setClienteId(clienteId);
        tablero.setTotalVisitas(25);
        tablero.setPuntosActuales(1250);
        tablero.setPuntosAcumulados(5000);
        tablero.setBeneficiosDisponibles(8);
        tablero.setTotalCanjes(12);
        tablero.setNivelFidelidad("GOLD");
        tablero.setSucursalFavorita("sucursal_001");
        tablero.setUltimaVisita("2024-01-15");
        tablero.setEstadoSincronizacion("SINCRONIZADO");
        tablero.setVersion(1);
        tablero.setUltimaModificacion(System.currentTimeMillis());
        return tablero;
    }

    private ApiService.TableroCliente crearTableroApiPrueba() {
        return new ApiService.TableroCliente(
            "cliente_001",
            25,
            1250,
            5000,
            8,
            12,
            "GOLD",
            "sucursal_001",
            "2024-01-15",
            5,
            10,
            6,
            2000,
            System.currentTimeMillis()
        );
    }
}