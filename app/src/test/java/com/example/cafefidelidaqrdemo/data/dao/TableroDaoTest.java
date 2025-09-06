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
import com.example.cafefidelidaqrdemo.data.entities.TableroEntity;

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
public class TableroDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase database;
    private TableroDao tableroDao;

    @Mock
    private Observer<List<TableroEntity>> listObserver;

    @Mock
    private Observer<TableroEntity> entityObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase.class
        ).allowMainThreadQueries().build();
        
        tableroDao = database.tableroDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testInsertarTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        
        // Act
        tableroDao.insertar(tablero);
        
        // Assert
        TableroEntity tableroObtenido = tableroDao.obtenerPorId("tablero_001");
        assertNotNull(tableroObtenido);
        assertEquals("tablero_001", tableroObtenido.getId());
        assertEquals("cliente_001", tableroObtenido.getClienteId());
    }

    @Test
    public void testInsertarMultiplesTableros() {
        // Arrange
        List<TableroEntity> tableros = Arrays.asList(
            crearTableroPrueba("tablero_001", "cliente_001"),
            crearTableroPrueba("tablero_002", "cliente_002"),
            crearTableroPrueba("tablero_003", "cliente_003")
        );
        
        // Act
        tableroDao.insertarTodos(tableros);
        
        // Assert
        List<TableroEntity> tablerosObtenidos = getValue(tableroDao.obtenerTodos());
        assertEquals(3, tablerosObtenidos.size());
    }

    @Test
    public void testActualizarTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tableroDao.insertar(tablero);
        
        // Act
        tablero.setTotalVisitas(50);
        tablero.setPuntosActuales(2500);
        tableroDao.actualizar(tablero);
        
        // Assert
        TableroEntity tableroActualizado = tableroDao.obtenerPorId("tablero_001");
        assertEquals(50, tableroActualizado.getTotalVisitas());
        assertEquals(2500, tableroActualizado.getPuntosActuales());
    }

    @Test
    public void testEliminarTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tableroDao.insertar(tablero);
        
        // Act
        tableroDao.eliminar(tablero);
        
        // Assert
        TableroEntity tableroEliminado = tableroDao.obtenerPorId("tablero_001");
        assertNull(tableroEliminado);
    }

    @Test
    public void testObtenerPorCliente() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        TableroEntity tableroCliente = getValue(tableroDao.obtenerPorCliente("cliente_001"));
        
        // Assert
        assertNotNull(tableroCliente);
        assertEquals("cliente_001", tableroCliente.getClienteId());
    }

    @Test
    public void testObtenerPorNivelFidelidad() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroEntity> tablerosGold = getValue(tableroDao.obtenerPorNivelFidelidad("GOLD"));
        
        // Assert
        assertFalse(tablerosGold.isEmpty());
        for (TableroEntity tablero : tablerosGold) {
            assertEquals("GOLD", tablero.getNivelFidelidad());
        }
    }

    @Test
    public void testObtenerPorSucursalFavorita() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroEntity> tablerosSucursal = getValue(
            tableroDao.obtenerPorSucursalFavorita("sucursal_001")
        );
        
        // Assert
        assertFalse(tablerosSucursal.isEmpty());
        for (TableroEntity tablero : tablerosSucursal) {
            assertEquals("sucursal_001", tablero.getSucursalFavorita());
        }
    }

    @Test
    public void testObtenerClientesActivos() {
        // Arrange
        insertarTablerosPrueba();
        
        // Configurar algunos clientes como activos (última visita reciente)
        long ahora = System.currentTimeMillis();
        long hace15Dias = ahora - (15 * 24 * 60 * 60 * 1000);
        
        TableroEntity tableroActivo = tableroDao.obtenerPorId("tablero_001");
        tableroActivo.setTimestampUltimaVisita(hace15Dias);
        tableroDao.actualizar(tableroActivo);
        
        // Act
        List<TableroEntity> clientesActivos = getValue(tableroDao.obtenerClientesActivos());
        
        // Assert
        assertFalse(clientesActivos.isEmpty());
        for (TableroEntity tablero : clientesActivos) {
            assertTrue(tablero.esClienteActivo());
        }
    }

    @Test
    public void testObtenerClientesInactivos() {
        // Arrange
        insertarTablerosPrueba();
        
        // Configurar algunos clientes como inactivos (última visita antigua)
        long ahora = System.currentTimeMillis();
        long hace45Dias = ahora - (45 * 24 * 60 * 60 * 1000);
        
        TableroEntity tableroInactivo = tableroDao.obtenerPorId("tablero_001");
        tableroInactivo.setTimestampUltimaVisita(hace45Dias);
        tableroDao.actualizar(tableroInactivo);
        
        // Act
        List<TableroEntity> clientesInactivos = getValue(tableroDao.obtenerClientesInactivos());
        
        // Assert
        assertFalse(clientesInactivos.isEmpty());
        for (TableroEntity tablero : clientesInactivos) {
            assertFalse(tablero.esClienteActivo());
        }
    }

    @Test
    public void testObtenerEstadisticasGenerales() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        TableroDao.EstadisticasGenerales estadisticas = tableroDao.obtenerEstadisticasGenerales();
        
        // Assert
        assertNotNull(estadisticas);
        assertTrue(estadisticas.totalClientes > 0);
        assertTrue(estadisticas.totalVisitas > 0);
        assertTrue(estadisticas.totalPuntos > 0);
        assertTrue(estadisticas.totalCanjes > 0);
        assertTrue(estadisticas.promedioVisitasPorCliente >= 0);
    }

    @Test
    public void testObtenerEstadisticasPorNivel() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.EstadisticasPorNivel> estadisticasNivel = 
            tableroDao.obtenerEstadisticasPorNivel();
        
        // Assert
        assertNotNull(estadisticasNivel);
        assertFalse(estadisticasNivel.isEmpty());
        
        for (TableroDao.EstadisticasPorNivel estadistica : estadisticasNivel) {
            assertNotNull(estadistica.nivelFidelidad);
            assertTrue(estadistica.totalClientes > 0);
            assertTrue(estadistica.promedioVisitas >= 0);
            assertTrue(estadistica.promedioPuntos >= 0);
        }
    }

    @Test
    public void testObtenerEstadisticasPorSucursal() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.EstadisticasPorSucursal> estadisticasSucursal = 
            tableroDao.obtenerEstadisticasPorSucursal();
        
        // Assert
        assertNotNull(estadisticasSucursal);
        assertFalse(estadisticasSucursal.isEmpty());
        
        for (TableroDao.EstadisticasPorSucursal estadistica : estadisticasSucursal) {
            assertNotNull(estadistica.sucursalId);
            assertTrue(estadistica.clientesFrecuentes >= 0);
            assertTrue(estadistica.promedioVisitas >= 0);
        }
    }

    @Test
    public void testObtenerTopClientesPorVisitas() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.TopCliente> topClientes = tableroDao.obtenerTopClientesPorVisitas(5);
        
        // Assert
        assertNotNull(topClientes);
        assertTrue(topClientes.size() <= 5);
        
        // Verificar que están ordenados por visitas descendente
        for (int i = 1; i < topClientes.size(); i++) {
            assertTrue(topClientes.get(i-1).totalVisitas >= topClientes.get(i).totalVisitas);
        }
    }

    @Test
    public void testObtenerTopClientesPorPuntos() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.TopCliente> topClientes = tableroDao.obtenerTopClientesPorPuntos(3);
        
        // Assert
        assertNotNull(topClientes);
        assertTrue(topClientes.size() <= 3);
        
        // Verificar que están ordenados por puntos descendente
        for (int i = 1; i < topClientes.size(); i++) {
            assertTrue(topClientes.get(i-1).puntosAcumulados >= topClientes.get(i).puntosAcumulados);
        }
    }

    @Test
    public void testObtenerTopClientesPorCanjes() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.TopCliente> topClientes = tableroDao.obtenerTopClientesPorCanjes(5);
        
        // Assert
        assertNotNull(topClientes);
        assertTrue(topClientes.size() <= 5);
        
        // Verificar que están ordenados por canjes descendente
        for (int i = 1; i < topClientes.size(); i++) {
            assertTrue(topClientes.get(i-1).totalCanjes >= topClientes.get(i).totalCanjes);
        }
    }

    @Test
    public void testObtenerAnalisisActividad() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.AnalisisActividad> analisisActividad = 
            tableroDao.obtenerAnalisisActividad("2024-01-01", "2024-01-31");
        
        // Assert
        assertNotNull(analisisActividad);
        
        for (TableroDao.AnalisisActividad analisis : analisisActividad) {
            assertNotNull(analisis.clienteId);
            assertTrue(analisis.visitasEnPeriodo >= 0);
            assertTrue(analisis.canjesEnPeriodo >= 0);
            assertTrue(analisis.puntosGanados >= 0);
        }
    }

    @Test
    public void testObtenerRachasVisitas() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.RachaVisitas> rachas = tableroDao.obtenerRachasVisitas();
        
        // Assert
        assertNotNull(rachas);
        
        for (TableroDao.RachaVisitas racha : rachas) {
            assertNotNull(racha.clienteId);
            assertTrue(racha.rachaActual >= 0);
            assertTrue(racha.rachaMaxima >= 0);
            assertTrue(racha.rachaMaxima >= racha.rachaActual);
        }
    }

    @Test
    public void testObtenerTendenciaVisitas() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.TendenciaVisitas> tendencia = 
            tableroDao.obtenerTendenciaVisitas("2024-01-01", "2024-02-28");
        
        // Assert
        assertNotNull(tendencia);
        
        for (TableroDao.TendenciaVisitas punto : tendencia) {
            assertNotNull(punto.fecha);
            assertTrue(punto.totalVisitas >= 0);
            assertTrue(punto.clientesUnicos >= 0);
            assertTrue(punto.promedioVisitasPorCliente >= 0);
        }
    }

    @Test
    public void testObtenerDistribucionNiveles() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        List<TableroDao.DistribucionNiveles> distribucion = 
            tableroDao.obtenerDistribucionNiveles();
        
        // Assert
        assertNotNull(distribucion);
        assertFalse(distribucion.isEmpty());
        
        int totalClientes = 0;
        for (TableroDao.DistribucionNiveles nivel : distribucion) {
            assertNotNull(nivel.nivelFidelidad);
            assertTrue(nivel.cantidadClientes > 0);
            assertTrue(nivel.porcentaje >= 0 && nivel.porcentaje <= 100);
            totalClientes += nivel.cantidadClientes;
        }
        
        assertTrue(totalClientes > 0);
    }

    @Test
    public void testObtenerTablerosPendientesSincronizacion() {
        // Arrange
        TableroEntity tableroPendiente = crearTableroPrueba("tablero_pendiente", "cliente_001");
        tableroPendiente.setEstadoSincronizacion("PENDIENTE");
        tableroDao.insertar(tableroPendiente);
        
        TableroEntity tableroSincronizado = crearTableroPrueba("tablero_sincronizado", "cliente_002");
        tableroSincronizado.setEstadoSincronizacion("SINCRONIZADO");
        tableroDao.insertar(tableroSincronizado);
        
        // Act
        List<TableroEntity> tablerosPendientes = getValue(
            tableroDao.obtenerTablerosPendientesSincronizacion()
        );
        
        // Assert
        assertEquals(1, tablerosPendientes.size());
        assertEquals("tablero_pendiente", tablerosPendientes.get(0).getId());
        assertEquals("PENDIENTE", tablerosPendientes.get(0).getEstadoSincronizacion());
    }

    @Test
    public void testObtenerTablerosConError() {
        // Arrange
        TableroEntity tableroError = crearTableroPrueba("tablero_error", "cliente_001");
        tableroError.setEstadoSincronizacion("ERROR");
        tableroDao.insertar(tableroError);
        
        TableroEntity tableroOk = crearTableroPrueba("tablero_ok", "cliente_002");
        tableroOk.setEstadoSincronizacion("SINCRONIZADO");
        tableroDao.insertar(tableroOk);
        
        // Act
        List<TableroEntity> tablerosError = getValue(tableroDao.obtenerTablerosConError());
        
        // Assert
        assertEquals(1, tablerosError.size());
        assertEquals("tablero_error", tablerosError.get(0).getId());
        assertEquals("ERROR", tablerosError.get(0).getEstadoSincronizacion());
    }

    @Test
    public void testMarcarComoSincronizado() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tablero.setEstadoSincronizacion("PENDIENTE");
        tableroDao.insertar(tablero);
        
        // Act
        tableroDao.marcarComoSincronizado("tablero_001");
        
        // Assert
        TableroEntity tableroActualizado = tableroDao.obtenerPorId("tablero_001");
        assertEquals("SINCRONIZADO", tableroActualizado.getEstadoSincronizacion());
        assertTrue(tableroActualizado.getUltimaSincronizacion() > 0);
    }

    @Test
    public void testMarcarComoError() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tablero.setEstadoSincronizacion("PENDIENTE");
        tableroDao.insertar(tablero);
        
        String mensajeError = "Error de sincronización";
        
        // Act
        tableroDao.marcarComoError("tablero_001", mensajeError);
        
        // Assert
        TableroEntity tableroActualizado = tableroDao.obtenerPorId("tablero_001");
        assertEquals("ERROR", tableroActualizado.getEstadoSincronizacion());
        assertEquals(mensajeError, tableroActualizado.getMensajeError());
    }

    @Test
    public void testActualizarEstadisticasCliente() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tableroDao.insertar(tablero);
        
        // Act
        tableroDao.actualizarEstadisticasCliente("cliente_001", 50, 2500, 15);
        
        // Assert
        TableroEntity tableroActualizado = tableroDao.obtenerPorId("tablero_001");
        assertEquals(50, tableroActualizado.getTotalVisitas());
        assertEquals(2500, tableroActualizado.getPuntosActuales());
        assertEquals(15, tableroActualizado.getTotalCanjes());
    }

    @Test
    public void testActualizarUltimaVisita() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tableroDao.insertar(tablero);
        
        long timestampAntes = System.currentTimeMillis();
        
        // Act
        tableroDao.actualizarUltimaVisita("cliente_001");
        
        // Assert
        TableroEntity tableroActualizado = tableroDao.obtenerPorId("tablero_001");
        assertTrue(tableroActualizado.getTimestampUltimaVisita() >= timestampAntes);
    }

    @Test
    public void testLimpiarCacheAntiguo() {
        // Arrange
        long ahora = System.currentTimeMillis();
        long hace2Horas = ahora - (2 * 60 * 60 * 1000);
        
        TableroEntity tableroAntiguo = crearTableroPrueba("tablero_antiguo", "cliente_001");
        tableroAntiguo.setUltimaModificacion(hace2Horas);
        tableroDao.insertar(tableroAntiguo);
        
        TableroEntity tableroReciente = crearTableroPrueba("tablero_reciente", "cliente_002");
        tableroReciente.setUltimaModificacion(ahora);
        tableroDao.insertar(tableroReciente);
        
        // Act
        int eliminados = tableroDao.limpiarCacheAntiguo(60 * 60 * 1000); // 1 hora
        
        // Assert
        assertEquals(1, eliminados);
        assertNull(tableroDao.obtenerPorId("tablero_antiguo"));
        assertNotNull(tableroDao.obtenerPorId("tablero_reciente"));
    }

    @Test
    public void testContarTableros() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        int totalTableros = tableroDao.contarTableros();
        
        // Assert
        assertTrue(totalTableros > 0);
    }

    @Test
    public void testContarClientesPorNivel() {
        // Arrange
        insertarTablerosPrueba();
        
        // Act
        int clientesGold = tableroDao.contarClientesPorNivel("GOLD");
        
        // Assert
        assertTrue(clientesGold >= 0);
    }

    @Test
    public void testExisteTablero() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tableroDao.insertar(tablero);
        
        // Act & Assert
        assertTrue(tableroDao.existeTablero("tablero_001"));
        assertFalse(tableroDao.existeTablero("tablero_inexistente"));
    }

    @Test
    public void testExisteTableroParaCliente() {
        // Arrange
        TableroEntity tablero = crearTableroPrueba("tablero_001", "cliente_001");
        tableroDao.insertar(tablero);
        
        // Act & Assert
        assertTrue(tableroDao.existeTableroParaCliente("cliente_001"));
        assertFalse(tableroDao.existeTableroParaCliente("cliente_inexistente"));
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
        tablero.setVisitasConsecutivas(5);
        tablero.setRachaMaxima(10);
        tablero.setMesesActivo(6);
        tablero.setPuntosProximoNivel(2000);
        tablero.setTimestampUltimaVisita(System.currentTimeMillis());
        tablero.setEstadoSincronizacion("PENDIENTE");
        tablero.setVersion(1);
        tablero.setUltimaModificacion(System.currentTimeMillis());
        return tablero;
    }

    private void insertarTablerosPrueba() {
        List<TableroEntity> tableros = Arrays.asList(
            crearTableroPrueba("tablero_001", "cliente_001"),
            crearTableroPrueba("tablero_002", "cliente_002"),
            crearTableroPrueba("tablero_003", "cliente_003"),
            crearTableroPrueba("tablero_004", "cliente_004")
        );
        
        // Configurar diferentes niveles de fidelidad
        tableros.get(0).setNivelFidelidad("GOLD");
        tableros.get(1).setNivelFidelidad("SILVER");
        tableros.get(2).setNivelFidelidad("BRONZE");
        tableros.get(3).setNivelFidelidad("GOLD");
        
        // Configurar diferentes sucursales favoritas
        tableros.get(0).setSucursalFavorita("sucursal_001");
        tableros.get(1).setSucursalFavorita("sucursal_002");
        tableros.get(2).setSucursalFavorita("sucursal_001");
        tableros.get(3).setSucursalFavorita("sucursal_003");
        
        tableroDao.insertarTodos(tableros);
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