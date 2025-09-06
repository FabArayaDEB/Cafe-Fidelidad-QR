package com.example.cafefidelidaqrdemo.data.entities;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class ReporteEntityTest {

    private ReporteEntity reporte;
    private final String TEST_ID = "test_id_123";
    private final String TEST_TIPO = "VISITAS";
    private final String TEST_FECHA = "2024-01-15";
    private final int TEST_VISITAS = 150;
    private final int TEST_CANJES = 75;
    private final int TEST_CLIENTES = 30;
    private final double TEST_VALOR = 2250.50;
    private final String TEST_SUCURSAL = "sucursal_001";
    private final String TEST_BENEFICIO = "beneficio_001";

    @Before
    public void setUp() {
        reporte = new ReporteEntity();
    }

    @Test
    public void testConstructorVacio() {
        // Act
        ReporteEntity nuevoReporte = new ReporteEntity();
        
        // Assert
        assertNotNull(nuevoReporte);
        assertNull(nuevoReporte.getId());
        assertNull(nuevoReporte.getTipo());
        assertEquals(0, nuevoReporte.getTotalVisitas());
        assertEquals(0, nuevoReporte.getTotalCanjes());
        assertEquals(0, nuevoReporte.getClientesUnicos());
        assertEquals(0.0, nuevoReporte.getValorTotalCanjes(), 0.01);
    }

    @Test
    public void testConstructorCompleto() {
        // Act
        ReporteEntity nuevoReporte = new ReporteEntity(TEST_ID, TEST_TIPO, TEST_FECHA, 
            TEST_VISITAS, TEST_CANJES, TEST_CLIENTES, TEST_VALOR, TEST_SUCURSAL, TEST_BENEFICIO);
        
        // Assert
        assertNotNull(nuevoReporte);
        assertEquals(TEST_ID, nuevoReporte.getId());
        assertEquals(TEST_TIPO, nuevoReporte.getTipo());
        assertEquals(TEST_FECHA, nuevoReporte.getFechaGeneracion());
        assertEquals(TEST_VISITAS, nuevoReporte.getTotalVisitas());
        assertEquals(TEST_CANJES, nuevoReporte.getTotalCanjes());
        assertEquals(TEST_CLIENTES, nuevoReporte.getClientesUnicos());
        assertEquals(TEST_VALOR, nuevoReporte.getValorTotalCanjes(), 0.01);
        assertEquals(TEST_SUCURSAL, nuevoReporte.getSucursalId());
        assertEquals(TEST_BENEFICIO, nuevoReporte.getBeneficioId());
    }

    @Test
    public void testSettersYGetters() {
        // Act
        reporte.setId(TEST_ID);
        reporte.setTipo(TEST_TIPO);
        reporte.setFechaGeneracion(TEST_FECHA);
        reporte.setTotalVisitas(TEST_VISITAS);
        reporte.setTotalCanjes(TEST_CANJES);
        reporte.setClientesUnicos(TEST_CLIENTES);
        reporte.setValorTotalCanjes(TEST_VALOR);
        reporte.setSucursalId(TEST_SUCURSAL);
        reporte.setBeneficioId(TEST_BENEFICIO);
        
        // Assert
        assertEquals(TEST_ID, reporte.getId());
        assertEquals(TEST_TIPO, reporte.getTipo());
        assertEquals(TEST_FECHA, reporte.getFechaGeneracion());
        assertEquals(TEST_VISITAS, reporte.getTotalVisitas());
        assertEquals(TEST_CANJES, reporte.getTotalCanjes());
        assertEquals(TEST_CLIENTES, reporte.getClientesUnicos());
        assertEquals(TEST_VALOR, reporte.getValorTotalCanjes(), 0.01);
        assertEquals(TEST_SUCURSAL, reporte.getSucursalId());
        assertEquals(TEST_BENEFICIO, reporte.getBeneficioId());
    }

    @Test
    public void testVersionControl() {
        // Arrange
        long timestampInicial = System.currentTimeMillis();
        
        // Act
        reporte.setVersion(1);
        reporte.setUltimaModificacion(timestampInicial);
        reporte.setUltimaSincronizacion(timestampInicial - 1000);
        
        // Assert
        assertEquals(1, reporte.getVersion());
        assertEquals(timestampInicial, reporte.getUltimaModificacion());
        assertEquals(timestampInicial - 1000, reporte.getUltimaSincronizacion());
    }

    @Test
    public void testEstadoSincronizacion() {
        // Act & Assert - Estado inicial
        assertEquals("PENDIENTE", reporte.getEstadoSincronizacion());
        
        // Act - Cambiar estado
        reporte.setEstadoSincronizacion("SINCRONIZADO");
        
        // Assert
        assertEquals("SINCRONIZADO", reporte.getEstadoSincronizacion());
        
        // Act - Cambiar a error
        reporte.setEstadoSincronizacion("ERROR");
        
        // Assert
        assertEquals("ERROR", reporte.getEstadoSincronizacion());
    }

    @Test
    public void testCalcularTasaConversion() {
        // Arrange
        reporte.setTotalVisitas(100);
        reporte.setTotalCanjes(25);
        
        // Act
        double tasaConversion = reporte.calcularTasaConversion();
        
        // Assert
        assertEquals(25.0, tasaConversion, 0.01);
    }

    @Test
    public void testCalcularTasaConversion_SinVisitas() {
        // Arrange
        reporte.setTotalVisitas(0);
        reporte.setTotalCanjes(25);
        
        // Act
        double tasaConversion = reporte.calcularTasaConversion();
        
        // Assert
        assertEquals(0.0, tasaConversion, 0.01);
    }

    @Test
    public void testCalcularValorPromedioCanje() {
        // Arrange
        reporte.setTotalCanjes(50);
        reporte.setValorTotalCanjes(2500.0);
        
        // Act
        double valorPromedio = reporte.calcularValorPromedioCanje();
        
        // Assert
        assertEquals(50.0, valorPromedio, 0.01);
    }

    @Test
    public void testCalcularValorPromedioCanje_SinCanjes() {
        // Arrange
        reporte.setTotalCanjes(0);
        reporte.setValorTotalCanjes(2500.0);
        
        // Act
        double valorPromedio = reporte.calcularValorPromedioCanje();
        
        // Assert
        assertEquals(0.0, valorPromedio, 0.01);
    }

    @Test
    public void testCalcularPromedioVisitasPorCliente() {
        // Arrange
        reporte.setTotalVisitas(300);
        reporte.setClientesUnicos(50);
        
        // Act
        double promedioVisitas = reporte.calcularPromedioVisitasPorCliente();
        
        // Assert
        assertEquals(6.0, promedioVisitas, 0.01);
    }

    @Test
    public void testCalcularPromedioVisitasPorCliente_SinClientes() {
        // Arrange
        reporte.setTotalVisitas(300);
        reporte.setClientesUnicos(0);
        
        // Act
        double promedioVisitas = reporte.calcularPromedioVisitasPorCliente();
        
        // Assert
        assertEquals(0.0, promedioVisitas, 0.01);
    }

    @Test
    public void testCalcularPromedioCanjesPorCliente() {
        // Arrange
        reporte.setTotalCanjes(100);
        reporte.setClientesUnicos(25);
        
        // Act
        double promedioCanjes = reporte.calcularPromedioCanjesPorCliente();
        
        // Assert
        assertEquals(4.0, promedioCanjes, 0.01);
    }

    @Test
    public void testCalcularPromedioCanjesPorCliente_SinClientes() {
        // Arrange
        reporte.setTotalCanjes(100);
        reporte.setClientesUnicos(0);
        
        // Act
        double promedioCanjes = reporte.calcularPromedioCanjesPorCliente();
        
        // Assert
        assertEquals(0.0, promedioCanjes, 0.01);
    }

    @Test
    public void testNecesitaSincronizacion_Verdadero() {
        // Arrange
        long ahora = System.currentTimeMillis();
        reporte.setUltimaModificacion(ahora);
        reporte.setUltimaSincronizacion(ahora - 10000); // 10 segundos antes
        
        // Act
        boolean necesitaSincronizacion = reporte.necesitaSincronizacion();
        
        // Assert
        assertTrue(necesitaSincronizacion);
    }

    @Test
    public void testNecesitaSincronizacion_Falso() {
        // Arrange
        long ahora = System.currentTimeMillis();
        reporte.setUltimaModificacion(ahora - 10000); // 10 segundos antes
        reporte.setUltimaSincronizacion(ahora);
        
        // Act
        boolean necesitaSincronizacion = reporte.necesitaSincronizacion();
        
        // Assert
        assertFalse(necesitaSincronizacion);
    }

    @Test
    public void testNecesitaSincronizacion_SinSincronizacionPrevia() {
        // Arrange
        reporte.setUltimaModificacion(System.currentTimeMillis());
        reporte.setUltimaSincronizacion(0); // Nunca sincronizado
        
        // Act
        boolean necesitaSincronizacion = reporte.necesitaSincronizacion();
        
        // Assert
        assertTrue(necesitaSincronizacion);
    }

    @Test
    public void testMarcarComoSincronizado() {
        // Arrange
        long timestampAntes = System.currentTimeMillis();
        reporte.setEstadoSincronizacion("PENDIENTE");
        
        // Act
        reporte.marcarComoSincronizado();
        
        // Assert
        assertEquals("SINCRONIZADO", reporte.getEstadoSincronizacion());
        assertTrue(reporte.getUltimaSincronizacion() >= timestampAntes);
    }

    @Test
    public void testMarcarComoError() {
        // Arrange
        String mensajeError = "Error de conexión";
        
        // Act
        reporte.marcarComoError(mensajeError);
        
        // Assert
        assertEquals("ERROR", reporte.getEstadoSincronizacion());
        assertEquals(mensajeError, reporte.getMensajeError());
    }

    @Test
    public void testActualizarVersion() {
        // Arrange
        reporte.setVersion(1);
        long timestampAntes = System.currentTimeMillis();
        
        // Act
        reporte.actualizarVersion();
        
        // Assert
        assertEquals(2, reporte.getVersion());
        assertTrue(reporte.getUltimaModificacion() >= timestampAntes);
    }

    @Test
    public void testEsValido_Verdadero() {
        // Arrange
        configurarReporteValido();
        
        // Act
        boolean esValido = reporte.esValido();
        
        // Assert
        assertTrue(esValido);
    }

    @Test
    public void testEsValido_SinId() {
        // Arrange
        configurarReporteValido();
        reporte.setId(null);
        
        // Act
        boolean esValido = reporte.esValido();
        
        // Assert
        assertFalse(esValido);
    }

    @Test
    public void testEsValido_SinTipo() {
        // Arrange
        configurarReporteValido();
        reporte.setTipo(null);
        
        // Act
        boolean esValido = reporte.esValido();
        
        // Assert
        assertFalse(esValido);
    }

    @Test
    public void testEsValido_ValoresNegativos() {
        // Arrange
        configurarReporteValido();
        reporte.setTotalVisitas(-1);
        
        // Act
        boolean esValido = reporte.esValido();
        
        // Assert
        assertFalse(esValido);
    }

    @Test
    public void testToString() {
        // Arrange
        configurarReporteValido();
        
        // Act
        String resultado = reporte.toString();
        
        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains(TEST_ID));
        assertTrue(resultado.contains(TEST_TIPO));
        assertTrue(resultado.contains(String.valueOf(TEST_VISITAS)));
    }

    @Test
    public void testEquals_MismoObjeto() {
        // Act & Assert
        assertEquals(reporte, reporte);
    }

    @Test
    public void testEquals_ObjetosIguales() {
        // Arrange
        ReporteEntity reporte1 = new ReporteEntity();
        ReporteEntity reporte2 = new ReporteEntity();
        reporte1.setId(TEST_ID);
        reporte2.setId(TEST_ID);
        
        // Act & Assert
        assertEquals(reporte1, reporte2);
    }

    @Test
    public void testEquals_ObjetosDiferentes() {
        // Arrange
        ReporteEntity reporte1 = new ReporteEntity();
        ReporteEntity reporte2 = new ReporteEntity();
        reporte1.setId("id1");
        reporte2.setId("id2");
        
        // Act & Assert
        assertNotEquals(reporte1, reporte2);
    }

    @Test
    public void testEquals_ObjetoNulo() {
        // Act & Assert
        assertNotEquals(reporte, null);
    }

    @Test
    public void testEquals_ClaseDiferente() {
        // Act & Assert
        assertNotEquals(reporte, "string");
    }

    @Test
    public void testHashCode_ObjetosIguales() {
        // Arrange
        ReporteEntity reporte1 = new ReporteEntity();
        ReporteEntity reporte2 = new ReporteEntity();
        reporte1.setId(TEST_ID);
        reporte2.setId(TEST_ID);
        
        // Act & Assert
        assertEquals(reporte1.hashCode(), reporte2.hashCode());
    }

    @Test
    public void testCopy() {
        // Arrange
        configurarReporteValido();
        
        // Act
        ReporteEntity copia = reporte.copy();
        
        // Assert
        assertNotSame(reporte, copia);
        assertEquals(reporte.getId(), copia.getId());
        assertEquals(reporte.getTipo(), copia.getTipo());
        assertEquals(reporte.getTotalVisitas(), copia.getTotalVisitas());
        assertEquals(reporte.getTotalCanjes(), copia.getTotalCanjes());
        assertEquals(reporte.getValorTotalCanjes(), copia.getValorTotalCanjes(), 0.01);
    }

    // Método auxiliar para configurar un reporte válido
    private void configurarReporteValido() {
        reporte.setId(TEST_ID);
        reporte.setTipo(TEST_TIPO);
        reporte.setFechaGeneracion(TEST_FECHA);
        reporte.setTotalVisitas(TEST_VISITAS);
        reporte.setTotalCanjes(TEST_CANJES);
        reporte.setClientesUnicos(TEST_CLIENTES);
        reporte.setValorTotalCanjes(TEST_VALOR);
        reporte.setSucursalId(TEST_SUCURSAL);
        reporte.setBeneficioId(TEST_BENEFICIO);
    }
}