package com.example.cafefidelidaqrdemo.ui.cliente.viewmodels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.cafefidelidaqrdemo.data.entities.TableroEntity;
import com.example.cafefidelidaqrdemo.data.repositories.TableroRepository;

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
public class TableroClienteViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private TableroRepository tableroRepository;

    @Mock
    private Observer<TableroEntity> tableroObserver;

    @Mock
    private Observer<TableroClienteViewModel.EstadoCarga> estadoObserver;

    @Mock
    private Observer<String> mensajeObserver;

    @Mock
    private Observer<Boolean> booleanObserver;

    @Mock
    private Observer<List<TableroClienteViewModel.CanjeReciente>> canjesObserver;

    @Mock
    private Observer<List<TableroClienteViewModel.VisitaReciente>> visitasObserver;

    @Mock
    private Observer<List<TableroClienteViewModel.BeneficioRecomendado>> beneficiosObserver;

    @Mock
    private Observer<TableroClienteViewModel.KPIsCliente> kpisObserver;

    private TableroClienteViewModel viewModel;
    private MutableLiveData<TableroEntity> tableroLiveData;
    private MutableLiveData<TableroRepository.EstadoCarga> estadoCargaLiveData;
    private MutableLiveData<String> mensajeErrorLiveData;
    private String clienteId = "cliente_001";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        tableroLiveData = new MutableLiveData<>();
        estadoCargaLiveData = new MutableLiveData<>();
        mensajeErrorLiveData = new MutableLiveData<>();
        
        // Configurar mocks del repositorio
        when(tableroRepository.obtenerTableroCliente(clienteId)).thenReturn(tableroLiveData);
        when(tableroRepository.getEstadoCarga()).thenReturn(estadoCargaLiveData);
        when(tableroRepository.getMensajeError()).thenReturn(mensajeErrorLiveData);
        
        viewModel = new TableroClienteViewModel(tableroRepository, clienteId);
    }

    @Test
    public void testInicializacionViewModel() {
        // Assert
        assertNotNull(viewModel.getTableroCliente());
        assertNotNull(viewModel.getEstadoCarga());
        assertNotNull(viewModel.getMensajeError());
        assertNotNull(viewModel.getKPIsCliente());
        assertNotNull(viewModel.getCanjesRecientes());
        assertNotNull(viewModel.getVisitasRecientes());
        assertNotNull(viewModel.getBeneficiosRecomendados());
        assertNotNull(viewModel.isRefrescando());
        assertNotNull(viewModel.isAutoRefreshActivo());
        assertEquals(clienteId, viewModel.getClienteId());
    }

    @Test
    public void testObtenerTableroCliente() {
        // Arrange
        TableroEntity tableroEsperado = crearTableroPrueba();
        
        // Act
        LiveData<TableroEntity> tablero = viewModel.getTableroCliente();
        tablero.observeForever(tableroObserver);
        tableroLiveData.setValue(tableroEsperado);
        
        // Assert
        verify(tableroRepository).obtenerTableroCliente(clienteId);
        verify(tableroObserver).onChanged(tableroEsperado);
    }

    @Test
    public void testRefrescarTablero() {
        // Act
        viewModel.refrescarTablero();
        
        // Assert
        verify(tableroRepository).refrescarTableroCliente(clienteId);
        
        // Verificar estado de refresco
        LiveData<Boolean> refrescando = viewModel.isRefrescando();
        refrescando.observeForever(booleanObserver);
    }

    @Test
    public void testCanjearBeneficio_Exitoso() throws InterruptedException {
        // Arrange
        String beneficioId = "beneficio_001";
        int puntos = 500;
        
        CountDownLatch latch = new CountDownLatch(1);
        
        // Simular canje exitoso
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tableroRepository).canjearBeneficio(clienteId, beneficioId, puntos);
        
        // Act
        viewModel.canjearBeneficio(beneficioId, puntos);
        
        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        verify(tableroRepository).canjearBeneficio(clienteId, beneficioId, puntos);
    }

    @Test
    public void testCanjearBeneficio_PuntosInsuficientes() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setPuntosActuales(300); // Menos puntos que los requeridos
        tableroLiveData.setValue(tablero);
        
        String beneficioId = "beneficio_001";
        int puntosRequeridos = 500;
        
        // Act
        boolean puedeCanjar = viewModel.validarPuntosParaCanje(puntosRequeridos);
        
        // Assert
        assertFalse(puedeCanjar);
    }

    @Test
    public void testCanjearBeneficio_PuntosSuficientes() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setPuntosActuales(1000); // Más puntos que los requeridos
        tableroLiveData.setValue(tablero);
        
        String beneficioId = "beneficio_001";
        int puntosRequeridos = 500;
        
        // Act
        boolean puedeCanjar = viewModel.validarPuntosParaCanje(puntosRequeridos);
        
        // Assert
        assertTrue(puedeCanjar);
    }

    @Test
    public void testRegistrarVisita() {
        // Arrange
        String sucursalId = "sucursal_001";
        
        // Act
        viewModel.registrarVisita(sucursalId);
        
        // Assert
        verify(tableroRepository).registrarVisita(clienteId, sucursalId);
    }

    @Test
    public void testActualizarMeta() {
        // Arrange
        String tipoMeta = "VISITAS";
        int valorMeta = 50;
        
        // Act
        viewModel.actualizarMeta(tipoMeta, valorMeta);
        
        // Assert
        verify(tableroRepository).actualizarMeta(clienteId, tipoMeta, valorMeta);
    }

    @Test
    public void testCalcularKPIs() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setTotalVisitas(25);
        tablero.setPuntosActuales(1250);
        tablero.setBeneficiosDisponibles(8);
        tablero.setTotalCanjes(12);
        
        // Act
        viewModel.calcularKPIs(tablero);
        
        // Assert
        LiveData<TableroClienteViewModel.KPIsCliente> kpis = viewModel.getKPIsCliente();
        kpis.observeForever(kpisObserver);
        
        verify(kpisObserver).onChanged(any(TableroClienteViewModel.KPIsCliente.class));
    }

    @Test
    public void testObtenerCanjesRecientes() {
        // Act
        viewModel.obtenerCanjesRecientes();
        
        // Assert
        verify(tableroRepository).obtenerCanjesRecientes(clienteId);
    }

    @Test
    public void testObtenerVisitasRecientes() {
        // Act
        viewModel.obtenerVisitasRecientes();
        
        // Assert
        verify(tableroRepository).obtenerVisitasRecientes(clienteId);
    }

    @Test
    public void testObtenerBeneficiosRecomendados() {
        // Act
        viewModel.obtenerBeneficiosRecomendados();
        
        // Assert
        verify(tableroRepository).obtenerBeneficiosRecomendados(clienteId);
    }

    @Test
    public void testIniciarAutoRefresh() {
        // Act
        viewModel.iniciarAutoRefresh(30000); // 30 segundos
        
        // Assert
        LiveData<Boolean> autoRefreshActivo = viewModel.isAutoRefreshActivo();
        autoRefreshActivo.observeForever(booleanObserver);
        
        assertTrue(viewModel.isAutoRefreshConfigurado());
    }

    @Test
    public void testDetenerAutoRefresh() {
        // Arrange
        viewModel.iniciarAutoRefresh(30000);
        
        // Act
        viewModel.detenerAutoRefresh();
        
        // Assert
        assertFalse(viewModel.isAutoRefreshConfigurado());
    }

    @Test
    public void testManejarEstadoCarga() {
        // Arrange
        TableroRepository.EstadoCarga estadoCargando = TableroRepository.EstadoCarga.CARGANDO;
        TableroRepository.EstadoCarga estadoExitoso = TableroRepository.EstadoCarga.EXITOSO;
        TableroRepository.EstadoCarga estadoError = TableroRepository.EstadoCarga.ERROR;
        
        // Act & Assert
        LiveData<TableroClienteViewModel.EstadoCarga> estado = viewModel.getEstadoCarga();
        estado.observeForever(estadoObserver);
        
        // Simular cambios de estado
        estadoCargaLiveData.setValue(estadoCargando);
        estadoCargaLiveData.setValue(estadoExitoso);
        estadoCargaLiveData.setValue(estadoError);
        
        verify(estadoObserver, times(3)).onChanged(any(TableroClienteViewModel.EstadoCarga.class));
    }

    @Test
    public void testManejarMensajesError() {
        // Arrange
        String mensajeError = "Error al cargar tablero";
        
        // Act
        LiveData<String> mensaje = viewModel.getMensajeError();
        mensaje.observeForever(mensajeObserver);
        mensajeErrorLiveData.setValue(mensajeError);
        
        // Assert
        verify(mensajeObserver).onChanged(mensajeError);
    }

    @Test
    public void testValidarNivelFidelidad() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setNivelFidelidad("GOLD");
        tableroLiveData.setValue(tablero);
        
        // Act
        String nivel = viewModel.obtenerNivelFidelidad();
        
        // Assert
        assertEquals("GOLD", nivel);
    }

    @Test
    public void testCalcularProgresoMeta() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setTotalVisitas(25);
        tablero.setMetaVisitas(50);
        
        // Act
        double progreso = viewModel.calcularProgresoMeta(tablero);
        
        // Assert
        assertEquals(50.0, progreso, 0.01); // 25/50 * 100 = 50%
    }

    @Test
    public void testValidarBeneficioDisponible() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setBeneficiosDisponibles(5);
        tableroLiveData.setValue(tablero);
        
        // Act
        boolean hayBeneficios = viewModel.tieneBeneficiosDisponibles();
        
        // Assert
        assertTrue(hayBeneficios);
    }

    @Test
    public void testCalcularDiasDesdeUltimaVisita() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        // Configurar fecha de última visita (hace 5 días)
        long hace5Dias = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000);
        tablero.setUltimaVisita(String.valueOf(hace5Dias));
        
        // Act
        int diasDesdeUltimaVisita = viewModel.calcularDiasDesdeUltimaVisita(tablero);
        
        // Assert
        assertEquals(5, diasDesdeUltimaVisita);
    }

    @Test
    public void testValidarRachaVisitas() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setRachaVisitas(7);
        
        // Act
        boolean tieneRacha = viewModel.tieneRachaActiva(tablero);
        
        // Assert
        assertTrue(tieneRacha);
    }

    @Test
    public void testObtenerRecomendacionesPersonalizadas() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba();
        tablero.setNivelFidelidad("SILVER");
        tablero.setPuntosActuales(800);
        
        // Act
        List<String> recomendaciones = viewModel.obtenerRecomendacionesPersonalizadas(tablero);
        
        // Assert
        assertNotNull(recomendaciones);
        assertFalse(recomendaciones.isEmpty());
    }

    @Test
    public void testManejarModoOffline() {
        // Arrange
        when(tableroRepository.validarConectividad()).thenReturn(false);
        
        // Act
        viewModel.manejarModoOffline();
        
        // Assert
        verify(tableroRepository).obtenerTableroCache(clienteId);
    }

    @Test
    public void testSincronizarCambiosPendientes() {
        // Act
        viewModel.sincronizarCambiosPendientes();
        
        // Assert
        verify(tableroRepository).sincronizarTablerosPendientes();
    }

    @Test
    public void testOnCleared() {
        // Arrange
        viewModel.iniciarAutoRefresh(30000);
        
        // Act
        viewModel.onCleared();
        
        // Assert
        assertFalse(viewModel.isAutoRefreshConfigurado());
    }

    @Test
    public void testKPIsCliente_Constructor() {
        // Arrange
        int totalVisitas = 25;
        int puntosActuales = 1250;
        int beneficiosDisponibles = 8;
        int totalCanjes = 12;
        double progresoMeta = 50.0;
        String nivelFidelidad = "GOLD";
        
        // Act
        TableroClienteViewModel.KPIsCliente kpis = new TableroClienteViewModel.KPIsCliente(
            totalVisitas, puntosActuales, beneficiosDisponibles, totalCanjes, progresoMeta, nivelFidelidad
        );
        
        // Assert
        assertEquals(totalVisitas, kpis.getTotalVisitas());
        assertEquals(puntosActuales, kpis.getPuntosActuales());
        assertEquals(beneficiosDisponibles, kpis.getBeneficiosDisponibles());
        assertEquals(totalCanjes, kpis.getTotalCanjes());
        assertEquals(progresoMeta, kpis.getProgresoMeta(), 0.01);
        assertEquals(nivelFidelidad, kpis.getNivelFidelidad());
    }

    @Test
    public void testCanjeReciente_Constructor() {
        // Arrange
        String beneficioNombre = "Café Gratis";
        int puntosUsados = 500;
        String fecha = "2024-01-15";
        String estado = "COMPLETADO";
        
        // Act
        TableroClienteViewModel.CanjeReciente canje = new TableroClienteViewModel.CanjeReciente(
            beneficioNombre, puntosUsados, fecha, estado
        );
        
        // Assert
        assertEquals(beneficioNombre, canje.getBeneficioNombre());
        assertEquals(puntosUsados, canje.getPuntosUsados());
        assertEquals(fecha, canje.getFecha());
        assertEquals(estado, canje.getEstado());
    }

    @Test
    public void testVisitaReciente_Constructor() {
        // Arrange
        String sucursalNombre = "Sucursal Centro";
        String fecha = "2024-01-15";
        int puntosGanados = 50;
        
        // Act
        TableroClienteViewModel.VisitaReciente visita = new TableroClienteViewModel.VisitaReciente(
            sucursalNombre, fecha, puntosGanados
        );
        
        // Assert
        assertEquals(sucursalNombre, visita.getSucursalNombre());
        assertEquals(fecha, visita.getFecha());
        assertEquals(puntosGanados, visita.getPuntosGanados());
    }

    @Test
    public void testBeneficioRecomendado_Constructor() {
        // Arrange
        String nombre = "Descuento 20%";
        String descripcion = "20% de descuento en tu próxima compra";
        int puntosRequeridos = 300;
        String categoria = "DESCUENTOS";
        boolean disponible = true;
        
        // Act
        TableroClienteViewModel.BeneficioRecomendado beneficio = new TableroClienteViewModel.BeneficioRecomendado(
            nombre, descripcion, puntosRequeridos, categoria, disponible
        );
        
        // Assert
        assertEquals(nombre, beneficio.getNombre());
        assertEquals(descripcion, beneficio.getDescripcion());
        assertEquals(puntosRequeridos, beneficio.getPuntosRequeridos());
        assertEquals(categoria, beneficio.getCategoria());
        assertEquals(disponible, beneficio.isDisponible());
    }

    @Test
    public void testEstadoCarga_Valores() {
        // Assert
        assertNotNull(TableroClienteViewModel.EstadoCarga.INICIAL);
        assertNotNull(TableroClienteViewModel.EstadoCarga.CARGANDO);
        assertNotNull(TableroClienteViewModel.EstadoCarga.EXITOSO);
        assertNotNull(TableroClienteViewModel.EstadoCarga.ERROR);
        assertNotNull(TableroClienteViewModel.EstadoCarga.SIN_DATOS);
    }

    @Test
    public void testManejoMemoria_LiberarRecursos() {
        // Arrange
        viewModel.iniciarAutoRefresh(30000);
        
        // Act
        viewModel.liberarRecursos();
        
        // Assert
        assertFalse(viewModel.isAutoRefreshConfigurado());
        verify(tableroRepository).cancelarOperacionesEnCurso();
    }

    @Test
    public void testValidacionesSeguridad_ClienteId() {
        // Arrange
        String clienteIdInvalido = null;
        
        // Act & Assert
        try {
            new TableroClienteViewModel(tableroRepository, clienteIdInvalido);
            fail("Debería lanzar excepción con clienteId nulo");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Cliente ID no puede ser nulo"));
        }
    }

    @Test
    public void testActualizacionEnTiempoReal() {
        // Arrange
        TableroEntity tableroInicial = crearTableroPrueba();
        TableroEntity tableroActualizado = crearTableroPrueba();
        tableroActualizado.setPuntosActuales(1500); // Más puntos
        
        // Act
        LiveData<TableroEntity> tablero = viewModel.getTableroCliente();
        tablero.observeForever(tableroObserver);
        
        tableroLiveData.setValue(tableroInicial);
        tableroLiveData.setValue(tableroActualizado);
        
        // Assert
        verify(tableroObserver, times(2)).onChanged(any(TableroEntity.class));
        verify(tableroObserver).onChanged(tableroActualizado);
    }

    // Métodos auxiliares
    private TableroEntity crearTableroPrueba() {
        TableroEntity tablero = new TableroEntity();
        tablero.setId("tablero_001");
        tablero.setClienteId(clienteId);
        tablero.setTotalVisitas(25);
        tablero.setPuntosActuales(1250);
        tablero.setPuntosAcumulados(5000);
        tablero.setBeneficiosDisponibles(8);
        tablero.setTotalCanjes(12);
        tablero.setNivelFidelidad("GOLD");
        tablero.setSucursalFavorita("sucursal_001");
        tablero.setUltimaVisita("2024-01-15");
        tablero.setMetaVisitas(50);
        tablero.setMetaPuntos(2000);
        tablero.setRachaVisitas(5);
        tablero.setEstadoSincronizacion("SINCRONIZADO");
        tablero.setVersion(1);
        tablero.setUltimaModificacion(System.currentTimeMillis());
        return tablero;
    }
}