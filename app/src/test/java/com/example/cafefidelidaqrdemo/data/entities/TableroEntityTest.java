package com.example.cafefidelidaqrdemo.data.entities;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class TableroEntityTest {

    private TableroEntity tablero;
    private final String TEST_ID = "test_tablero_123";
    private final String TEST_CLIENTE_ID = "cliente_001";
    private final int TEST_VISITAS = 25;
    private final int TEST_PUNTOS = 1250;
    private final int TEST_BENEFICIOS = 8;
    private final int TEST_CANJES = 12;
    private final String TEST_NIVEL = "GOLD";
    private final String TEST_SUCURSAL = "sucursal_001";
    private final String TEST_FECHA = "2024-01-15";

    @Before
    public void setUp() {
        tablero = new TableroEntity();
    }

    @Test
    public void testConstructorVacio() {
        // Act
        TableroEntity nuevoTablero = new TableroEntity();
        
        // Assert
        assertNotNull(nuevoTablero);
        assertNull(nuevoTablero.getId());
        assertNull(nuevoTablero.getClienteId());
        assertEquals(0, nuevoTablero.getTotalVisitas());
        assertEquals(0, nuevoTablero.getPuntosActuales());
        assertEquals(0, nuevoTablero.getBeneficiosDisponibles());
        assertEquals(0, nuevoTablero.getTotalCanjes());
    }

    @Test
    public void testConstructorCompleto() {
        // Act
        TableroEntity nuevoTablero = new TableroEntity(TEST_ID, TEST_CLIENTE_ID, TEST_VISITAS, 
            TEST_PUNTOS, TEST_BENEFICIOS, TEST_CANJES, TEST_NIVEL, TEST_SUCURSAL, TEST_FECHA);
        
        // Assert
        assertNotNull(nuevoTablero);
        assertEquals(TEST_ID, nuevoTablero.getId());
        assertEquals(TEST_CLIENTE_ID, nuevoTablero.getClienteId());
        assertEquals(TEST_VISITAS, nuevoTablero.getTotalVisitas());
        assertEquals(TEST_PUNTOS, nuevoTablero.getPuntosActuales());
        assertEquals(TEST_BENEFICIOS, nuevoTablero.getBeneficiosDisponibles());
        assertEquals(TEST_CANJES, nuevoTablero.getTotalCanjes());
        assertEquals(TEST_NIVEL, nuevoTablero.getNivelFidelidad());
        assertEquals(TEST_SUCURSAL, nuevoTablero.getSucursalFavorita());
        assertEquals(TEST_FECHA, nuevoTablero.getUltimaVisita());
    }

    @Test
    public void testSettersYGetters() {
        // Act
        tablero.setId(TEST_ID);
        tablero.setClienteId(TEST_CLIENTE_ID);
        tablero.setTotalVisitas(TEST_VISITAS);
        tablero.setPuntosActuales(TEST_PUNTOS);
        tablero.setBeneficiosDisponibles(TEST_BENEFICIOS);
        tablero.setTotalCanjes(TEST_CANJES);
        tablero.setNivelFidelidad(TEST_NIVEL);
        tablero.setSucursalFavorita(TEST_SUCURSAL);
        tablero.setUltimaVisita(TEST_FECHA);
        
        // Assert
        assertEquals(TEST_ID, tablero.getId());
        assertEquals(TEST_CLIENTE_ID, tablero.getClienteId());
        assertEquals(TEST_VISITAS, tablero.getTotalVisitas());
        assertEquals(TEST_PUNTOS, tablero.getPuntosActuales());
        assertEquals(TEST_BENEFICIOS, tablero.getBeneficiosDisponibles());
        assertEquals(TEST_CANJES, tablero.getTotalCanjes());
        assertEquals(TEST_NIVEL, tablero.getNivelFidelidad());
        assertEquals(TEST_SUCURSAL, tablero.getSucursalFavorita());
        assertEquals(TEST_FECHA, tablero.getUltimaVisita());
    }

    @Test
    public void testVersionControl() {
        // Arrange
        long timestampInicial = System.currentTimeMillis();
        
        // Act
        tablero.setVersion(1);
        tablero.setUltimaModificacion(timestampInicial);
        tablero.setUltimaSincronizacion(timestampInicial - 1000);
        
        // Assert
        assertEquals(1, tablero.getVersion());
        assertEquals(timestampInicial, tablero.getUltimaModificacion());
        assertEquals(timestampInicial - 1000, tablero.getUltimaSincronizacion());
    }

    @Test
    public void testEstadoSincronizacion() {
        // Act & Assert - Estado inicial
        assertEquals("PENDIENTE", tablero.getEstadoSincronizacion());
        
        // Act - Cambiar estado
        tablero.setEstadoSincronizacion("SINCRONIZADO");
        
        // Assert
        assertEquals("SINCRONIZADO", tablero.getEstadoSincronizacion());
        
        // Act - Cambiar a error
        tablero.setEstadoSincronizacion("ERROR");
        
        // Assert
        assertEquals("ERROR", tablero.getEstadoSincronizacion());
    }

    @Test
    public void testCalcularPromedioVisitasMensual() {
        // Arrange
        tablero.setTotalVisitas(120);
        tablero.setMesesActivo(12);
        
        // Act
        double promedioMensual = tablero.calcularPromedioVisitasMensual();
        
        // Assert
        assertEquals(10.0, promedioMensual, 0.01);
    }

    @Test
    public void testCalcularPromedioVisitasMensual_SinMesesActivos() {
        // Arrange
        tablero.setTotalVisitas(120);
        tablero.setMesesActivo(0);
        
        // Act
        double promedioMensual = tablero.calcularPromedioVisitasMensual();
        
        // Assert
        assertEquals(0.0, promedioMensual, 0.01);
    }

    @Test
    public void testCalcularTasaCanjes() {
        // Arrange
        tablero.setTotalCanjes(30);
        tablero.setTotalVisitas(100);
        
        // Act
        double tasaCanjes = tablero.calcularTasaCanjes();
        
        // Assert
        assertEquals(30.0, tasaCanjes, 0.01);
    }

    @Test
    public void testCalcularTasaCanjes_SinVisitas() {
        // Arrange
        tablero.setTotalCanjes(30);
        tablero.setTotalVisitas(0);
        
        // Act
        double tasaCanjes = tablero.calcularTasaCanjes();
        
        // Assert
        assertEquals(0.0, tasaCanjes, 0.01);
    }

    @Test
    public void testCalcularPromedioPuntosPorVisita() {
        // Arrange
        tablero.setPuntosAcumulados(5000);
        tablero.setTotalVisitas(50);
        
        // Act
        double promedioPuntos = tablero.calcularPromedioPuntosPorVisita();
        
        // Assert
        assertEquals(100.0, promedioPuntos, 0.01);
    }

    @Test
    public void testCalcularPromedioPuntosPorVisita_SinVisitas() {
        // Arrange
        tablero.setPuntosAcumulados(5000);
        tablero.setTotalVisitas(0);
        
        // Act
        double promedioPuntos = tablero.calcularPromedioPuntosPorVisita();
        
        // Assert
        assertEquals(0.0, promedioPuntos, 0.01);
    }

    @Test
    public void testCalcularPorcentajeProgresoNivel() {
        // Arrange
        tablero.setPuntosActuales(750);
        tablero.setPuntosProximoNivel(1000);
        
        // Act
        double porcentajeProgreso = tablero.calcularPorcentajeProgresoNivel();
        
        // Assert
        assertEquals(75.0, porcentajeProgreso, 0.01);
    }

    @Test
    public void testCalcularPorcentajeProgresoNivel_SinPuntosRequeridos() {
        // Arrange
        tablero.setPuntosActuales(750);
        tablero.setPuntosProximoNivel(0);
        
        // Act
        double porcentajeProgreso = tablero.calcularPorcentajeProgresoNivel();
        
        // Assert
        assertEquals(100.0, porcentajeProgreso, 0.01);
    }

    @Test
    public void testCalcularDiasDesdeUltimaVisita() {
        // Arrange
        long ahora = System.currentTimeMillis();
        long hace3Dias = ahora - (3 * 24 * 60 * 60 * 1000);
        tablero.setTimestampUltimaVisita(hace3Dias);
        
        // Act
        int diasDesdeUltimaVisita = tablero.calcularDiasDesdeUltimaVisita();
        
        // Assert
        assertEquals(3, diasDesdeUltimaVisita);
    }

    @Test
    public void testCalcularRachaVisitas() {
        // Arrange
        tablero.setVisitasConsecutivas(7);
        tablero.setRachaMaxima(10);
        
        // Act
        int rachaActual = tablero.getVisitasConsecutivas();
        int rachaMaxima = tablero.getRachaMaxima();
        
        // Assert
        assertEquals(7, rachaActual);
        assertEquals(10, rachaMaxima);
    }

    @Test
    public void testEsClienteActivo_Verdadero() {
        // Arrange
        long ahora = System.currentTimeMillis();
        long hace15Dias = ahora - (15 * 24 * 60 * 60 * 1000);
        tablero.setTimestampUltimaVisita(hace15Dias);
        
        // Act
        boolean esActivo = tablero.esClienteActivo();
        
        // Assert
        assertTrue(esActivo);
    }

    @Test
    public void testEsClienteActivo_Falso() {
        // Arrange
        long ahora = System.currentTimeMillis();
        long hace45Dias = ahora - (45 * 24 * 60 * 60 * 1000);
        tablero.setTimestampUltimaVisita(hace45Dias);
        
        // Act
        boolean esActivo = tablero.esClienteActivo();
        
        // Assert
        assertFalse(esActivo);
    }

    @Test
    public void testTieneBeneficiosDisponibles_Verdadero() {
        // Arrange
        tablero.setBeneficiosDisponibles(5);
        
        // Act
        boolean tieneBeneficios = tablero.tieneBeneficiosDisponibles();
        
        // Assert
        assertTrue(tieneBeneficios);
    }

    @Test
    public void testTieneBeneficiosDisponibles_Falso() {
        // Arrange
        tablero.setBeneficiosDisponibles(0);
        
        // Act
        boolean tieneBeneficios = tablero.tieneBeneficiosDisponibles();
        
        // Assert
        assertFalse(tieneBeneficios);
    }

    @Test
    public void testPuedeSubirNivel_Verdadero() {
        // Arrange
        tablero.setPuntosActuales(1000);
        tablero.setPuntosProximoNivel(800);
        
        // Act
        boolean puedeSubir = tablero.puedeSubirNivel();
        
        // Assert
        assertTrue(puedeSubir);
    }

    @Test
    public void testPuedeSubirNivel_Falso() {
        // Arrange
        tablero.setPuntosActuales(600);
        tablero.setPuntosProximoNivel(800);
        
        // Act
        boolean puedeSubir = tablero.puedeSubirNivel();
        
        // Assert
        assertFalse(puedeSubir);
    }

    @Test
    public void testNecesitaSincronizacion_Verdadero() {
        // Arrange
        long ahora = System.currentTimeMillis();
        tablero.setUltimaModificacion(ahora);
        tablero.setUltimaSincronizacion(ahora - 10000); // 10 segundos antes
        
        // Act
        boolean necesitaSincronizacion = tablero.necesitaSincronizacion();
        
        // Assert
        assertTrue(necesitaSincronizacion);
    }

    @Test
    public void testNecesitaSincronizacion_Falso() {
        // Arrange
        long ahora = System.currentTimeMillis();
        tablero.setUltimaModificacion(ahora - 10000); // 10 segundos antes
        tablero.setUltimaSincronizacion(ahora);
        
        // Act
        boolean necesitaSincronizacion = tablero.necesitaSincronizacion();
        
        // Assert
        assertFalse(necesitaSincronizacion);
    }

    @Test
    public void testMarcarComoSincronizado() {
        // Arrange
        long timestampAntes = System.currentTimeMillis();
        tablero.setEstadoSincronizacion("PENDIENTE");
        
        // Act
        tablero.marcarComoSincronizado();
        
        // Assert
        assertEquals("SINCRONIZADO", tablero.getEstadoSincronizacion());
        assertTrue(tablero.getUltimaSincronizacion() >= timestampAntes);
    }

    @Test
    public void testMarcarComoError() {
        // Arrange
        String mensajeError = "Error de sincronización";
        
        // Act
        tablero.marcarComoError(mensajeError);
        
        // Assert
        assertEquals("ERROR", tablero.getEstadoSincronizacion());
        assertEquals(mensajeError, tablero.getMensajeError());
    }

    @Test
    public void testActualizarVersion() {
        // Arrange
        tablero.setVersion(1);
        long timestampAntes = System.currentTimeMillis();
        
        // Act
        tablero.actualizarVersion();
        
        // Assert
        assertEquals(2, tablero.getVersion());
        assertTrue(tablero.getUltimaModificacion() >= timestampAntes);
    }

    @Test
    public void testRegistrarVisita() {
        // Arrange
        tablero.setTotalVisitas(10);
        tablero.setVisitasConsecutivas(3);
        tablero.setRachaMaxima(5);
        long timestampAntes = System.currentTimeMillis();
        
        // Act
        tablero.registrarVisita();
        
        // Assert
        assertEquals(11, tablero.getTotalVisitas());
        assertEquals(4, tablero.getVisitasConsecutivas());
        assertEquals(5, tablero.getRachaMaxima()); // No cambia porque 4 < 5
        assertTrue(tablero.getTimestampUltimaVisita() >= timestampAntes);
    }

    @Test
    public void testRegistrarVisita_NuevaRachaMaxima() {
        // Arrange
        tablero.setTotalVisitas(10);
        tablero.setVisitasConsecutivas(5);
        tablero.setRachaMaxima(5);
        
        // Act
        tablero.registrarVisita();
        
        // Assert
        assertEquals(11, tablero.getTotalVisitas());
        assertEquals(6, tablero.getVisitasConsecutivas());
        assertEquals(6, tablero.getRachaMaxima()); // Se actualiza porque 6 > 5
    }

    @Test
    public void testRegistrarCanje() {
        // Arrange
        tablero.setTotalCanjes(5);
        tablero.setPuntosActuales(1000);
        int puntosACanjear = 200;
        
        // Act
        tablero.registrarCanje(puntosACanjear);
        
        // Assert
        assertEquals(6, tablero.getTotalCanjes());
        assertEquals(800, tablero.getPuntosActuales());
    }

    @Test
    public void testAgregarPuntos() {
        // Arrange
        tablero.setPuntosActuales(500);
        tablero.setPuntosAcumulados(2000);
        int puntosAAgregar = 150;
        
        // Act
        tablero.agregarPuntos(puntosAAgregar);
        
        // Assert
        assertEquals(650, tablero.getPuntosActuales());
        assertEquals(2150, tablero.getPuntosAcumulados());
    }

    @Test
    public void testReiniciarRacha() {
        // Arrange
        tablero.setVisitasConsecutivas(7);
        
        // Act
        tablero.reiniciarRacha();
        
        // Assert
        assertEquals(0, tablero.getVisitasConsecutivas());
    }

    @Test
    public void testEsValido_Verdadero() {
        // Arrange
        configurarTableroValido();
        
        // Act
        boolean esValido = tablero.esValido();
        
        // Assert
        assertTrue(esValido);
    }

    @Test
    public void testEsValido_SinId() {
        // Arrange
        configurarTableroValido();
        tablero.setId(null);
        
        // Act
        boolean esValido = tablero.esValido();
        
        // Assert
        assertFalse(esValido);
    }

    @Test
    public void testEsValido_SinClienteId() {
        // Arrange
        configurarTableroValido();
        tablero.setClienteId(null);
        
        // Act
        boolean esValido = tablero.esValido();
        
        // Assert
        assertFalse(esValido);
    }

    @Test
    public void testEsValido_ValoresNegativos() {
        // Arrange
        configurarTableroValido();
        tablero.setPuntosActuales(-1);
        
        // Act
        boolean esValido = tablero.esValido();
        
        // Assert
        assertFalse(esValido);
    }

    @Test
    public void testToString() {
        // Arrange
        configurarTableroValido();
        
        // Act
        String resultado = tablero.toString();
        
        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains(TEST_ID));
        assertTrue(resultado.contains(TEST_CLIENTE_ID));
        assertTrue(resultado.contains(String.valueOf(TEST_VISITAS)));
    }

    @Test
    public void testEquals_MismoObjeto() {
        // Act & Assert
        assertEquals(tablero, tablero);
    }

    @Test
    public void testEquals_ObjetosIguales() {
        // Arrange
        TableroEntity tablero1 = new TableroEntity();
        TableroEntity tablero2 = new TableroEntity();
        tablero1.setId(TEST_ID);
        tablero2.setId(TEST_ID);
        
        // Act & Assert
        assertEquals(tablero1, tablero2);
    }

    @Test
    public void testEquals_ObjetosDiferentes() {
        // Arrange
        TableroEntity tablero1 = new TableroEntity();
        TableroEntity tablero2 = new TableroEntity();
        tablero1.setId("id1");
        tablero2.setId("id2");
        
        // Act & Assert
        assertNotEquals(tablero1, tablero2);
    }

    @Test
    public void testEquals_ObjetoNulo() {
        // Act & Assert
        assertNotEquals(tablero, null);
    }

    @Test
    public void testHashCode_ObjetosIguales() {
        // Arrange
        TableroEntity tablero1 = new TableroEntity();
        TableroEntity tablero2 = new TableroEntity();
        tablero1.setId(TEST_ID);
        tablero2.setId(TEST_ID);
        
        // Act & Assert
        assertEquals(tablero1.hashCode(), tablero2.hashCode());
    }

    @Test
    public void testCopy() {
        // Arrange
        configurarTableroValido();
        
        // Act
        TableroEntity copia = tablero.copy();
        
        // Assert
        assertNotSame(tablero, copia);
        assertEquals(tablero.getId(), copia.getId());
        assertEquals(tablero.getClienteId(), copia.getClienteId());
        assertEquals(tablero.getTotalVisitas(), copia.getTotalVisitas());
        assertEquals(tablero.getPuntosActuales(), copia.getPuntosActuales());
        assertEquals(tablero.getBeneficiosDisponibles(), copia.getBeneficiosDisponibles());
    }

    // Método auxiliar para configurar un tablero válido
    private void configurarTableroValido() {
        tablero.setId(TEST_ID);
        tablero.setClienteId(TEST_CLIENTE_ID);
        tablero.setTotalVisitas(TEST_VISITAS);
        tablero.setPuntosActuales(TEST_PUNTOS);
        tablero.setBeneficiosDisponibles(TEST_BENEFICIOS);
        tablero.setTotalCanjes(TEST_CANJES);
        tablero.setNivelFidelidad(TEST_NIVEL);
        tablero.setSucursalFavorita(TEST_SUCURSAL);
        tablero.setUltimaVisita(TEST_FECHA);
    }
}